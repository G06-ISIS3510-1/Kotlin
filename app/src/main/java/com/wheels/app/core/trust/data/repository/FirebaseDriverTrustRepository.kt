package com.wheels.app.core.trust.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wheels.app.core.trust.domain.model.DriverTrustScore
import com.wheels.app.core.trust.domain.model.TrustScoreNotice
import com.wheels.app.core.trust.domain.repository.DriverRideTrustActionParams
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseDriverTrustRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : DriverTrustRepository {

    override fun observeDriverTrustScore(userId: String): Flow<DriverTrustScore?> {
        val firestore = getFirestoreOrNull() ?: return flowOf(null)

        return callbackFlow {
            val registration = firestore
            .collection(TRUST_SCORES_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toDriverTrustScore())
            }

            awaitClose { registration.remove() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun startRide(params: DriverRideTrustActionParams) {
        withContext(ioDispatcher) {
            val firestore = requireFirestore()
            prepareRideDocument(params = params, fallbackStatus = RIDE_STATUS_PUBLISHED)
            firestore.collection(RIDES_COLLECTION)
                .document(params.rideId)
                .update(
                    mapOf(
                        "status" to RIDE_STATUS_IN_PROGRESS,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
        }
    }

    override suspend fun completeRideAndAwaitTrustUpdate(
        params: DriverRideTrustActionParams
    ): TrustScoreNotice = withContext(ioDispatcher) {
        val firestore = requireFirestore()
        val before = readTrustScoreOnce(params.driverId)
        prepareRideDocument(params = params, fallbackStatus = RIDE_STATUS_IN_PROGRESS)

        firestore.collection(RIDES_COLLECTION)
            .document(params.rideId)
            .update(
                mapOf(
                    "status" to RIDE_STATUS_COMPLETED,
                    "completedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .awaitResult()

        val after = awaitUpdatedTrustScore(params.driverId, before)

        TrustScoreNotice(
            title = "Trust score updated",
            message = "You earned +1 point for completing this ride. Your new reliability score is ${after.reliabilityScore}.",
            newScore = after.reliabilityScore,
            deltaPoints = 1
        )
    }

    override suspend fun cancelRideAndAwaitTrustUpdate(
        params: DriverRideTrustActionParams
    ): TrustScoreNotice = withContext(ioDispatcher) {
        val firestore = requireFirestore()
        val before = readTrustScoreOnce(params.driverId)
        val penalty = classifyLateCancellationPenalty(params.scheduledStartAtMillis)
        prepareRideDocument(params = params, fallbackStatus = RIDE_STATUS_PUBLISHED)

        firestore.collection(RIDES_COLLECTION)
            .document(params.rideId)
            .update(
                mapOf(
                    "status" to RIDE_STATUS_CANCELED,
                    "canceledByRole" to "driver",
                    "canceledAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .awaitResult()

        val after = awaitUpdatedTrustScore(params.driverId, before)

        TrustScoreNotice(
            title = "Trust score updated",
            message = "We subtracted ${penalty.penaltyPoints} points because you canceled this ride ${penalty.windowDescription}. Your new reliability score is ${after.reliabilityScore}.",
            newScore = after.reliabilityScore,
            deltaPoints = -penalty.penaltyPoints
        )
    }

    private suspend fun prepareRideDocument(
        params: DriverRideTrustActionParams,
        fallbackStatus: String
    ) {
        val firestore = requireFirestore()
        val rideRef = firestore.collection(RIDES_COLLECTION).document(params.rideId)
        val rideSnapshot = rideRef.get().awaitResult()
        val baseFields = mapOf(
            "driverId" to params.driverId,
            "scheduledStartAt" to Timestamp(Date(params.scheduledStartAtMillis)),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (rideSnapshot.exists()) {
            rideRef.set(baseFields, SetOptions.merge()).awaitResult()
        } else {
            rideRef.set(baseFields + ("status" to fallbackStatus)).awaitResult()
        }
    }

    private suspend fun readTrustScoreOnce(userId: String): DriverTrustScore? {
        val firestore = requireFirestore()
        return firestore.collection(TRUST_SCORES_COLLECTION)
            .document(userId)
            .get()
            .awaitResult()
            .toDriverTrustScore()
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? {
        val app = FirebaseApp.getApps(context).firstOrNull() ?: return null
        return FirebaseFirestore.getInstance(app)
    }

    private fun requireFirestore(): FirebaseFirestore {
        return getFirestoreOrNull()
            ?: throw IllegalStateException(
                "Firebase is not configured yet. Add app/google-services.json to enable trust score sync."
            )
    }

    private suspend fun awaitUpdatedTrustScore(
        userId: String,
        previousScore: DriverTrustScore?
    ): DriverTrustScore {
        repeat(16) {
            delay(500)
            val current = readTrustScoreOnce(userId)
            if (current != null && hasScoreChanged(previousScore, current)) {
                return current
            }
        }

        return readTrustScoreOnce(userId)
            ?: throw IllegalStateException("Trust score update did not arrive in time.")
    }

    private fun hasScoreChanged(
        previousScore: DriverTrustScore?,
        currentScore: DriverTrustScore
    ): Boolean {
        if (previousScore == null) {
            return true
        }

        return currentScore.updatedAtMillis != previousScore.updatedAtMillis ||
            currentScore.reliabilityScore != previousScore.reliabilityScore
    }

    private fun DocumentSnapshot.toDriverTrustScore(): DriverTrustScore? {
        if (!exists()) {
            return null
        }

        return DriverTrustScore(
            reliabilityScore = getLong("reliabilityScore")?.toInt() ?: return null,
            explanation = getString("explanation").orEmpty(),
            updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    private fun classifyLateCancellationPenalty(
        scheduledStartAtMillis: Long,
        canceledAtMillis: Long = System.currentTimeMillis()
    ): LocalCancellationPenalty {
        val hoursBeforeRide =
            (scheduledStartAtMillis - canceledAtMillis).toDouble() / (1000 * 60 * 60)

        return when {
            hoursBeforeRide > 12 -> LocalCancellationPenalty(2, "more than 12 hours before departure")
            hoursBeforeRide > 3 -> LocalCancellationPenalty(5, "between 3 and 12 hours before departure")
            hoursBeforeRide > 1 -> LocalCancellationPenalty(10, "between 1 and 3 hours before departure")
            else -> LocalCancellationPenalty(15, "less than 1 hour before departure")
        }
    }

    private data class LocalCancellationPenalty(
        val penaltyPoints: Int,
        val windowDescription: String
    )

    private companion object {
        const val RIDES_COLLECTION = "rides"
        const val TRUST_SCORES_COLLECTION = "trustScores"
        const val RIDE_STATUS_PUBLISHED = "published"
        const val RIDE_STATUS_IN_PROGRESS = "in_progress"
        const val RIDE_STATUS_COMPLETED = "completed"
        const val RIDE_STATUS_CANCELED = "canceled"
    }
}
