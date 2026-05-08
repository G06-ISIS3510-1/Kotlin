package com.wheels.app.features.reviews.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import com.wheels.app.features.reviews.domain.model.calculateDriverReviewSummary
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirestoreRideReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : RideReviewRepository {

    override fun observeDriverReviews(driverId: String): Flow<List<RideReview>> = callbackFlow {
        val registration = firestore
            .collection(REVIEWS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val reviews = snapshot
                        ?.documents
                        .orEmpty()
                        .mapNotNull { mapReviewDocument(it.id, it.data ?: emptyMap()) }
                        .filter { it.driverId == driverId }
                        .sortedByDescending { it.createdAt ?: Instant.EPOCH }

                    trySend(reviews)
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(ioDispatcher)

    override fun observeDriverReviewSummaries(): Flow<Map<String, DriverReviewSummary>> = callbackFlow {
        val registration = firestore
            .collection(REVIEWS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val reviews = snapshot
                        ?.documents
                        .orEmpty()
                        .mapNotNull { mapReviewDocument(it.id, it.data ?: emptyMap()) }

                    trySend(calculateDriverReviewSummary(reviews))
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun submitReview(request: SubmitRideReviewRequest): RideReview {
        val reviewId = buildReviewId(request.driverId, request.passengerId)
        val review = RideReview(
            reviewId = reviewId,
            rideId = request.rideId,
            driverId = request.driverId,
            driverName = request.driverName,
            passengerId = request.passengerId,
            passengerName = request.passengerName,
            stars = request.stars.coerceIn(0, 5),
            comment = request.comment.trim(),
            createdAt = Instant.now()
        )

        return withContext(ioDispatcher) {
            firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .set(buildReviewDocumentData(review))
                .awaitResult()
            review
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val REVIEWS_COLLECTION = "reviews"
    }
}

internal fun buildReviewDocumentData(review: RideReview): Map<String, Any?> {
    return linkedMapOf(
        "reviewId" to review.reviewId,
        "driverId" to review.driverId,
        "driverName" to review.driverName,
        "passengerId" to review.passengerId,
        "passengerName" to review.passengerName,
        "stars" to review.stars.coerceIn(0, 5),
        "comment" to review.comment.trim(),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

internal fun mapReviewDocument(
    reviewId: String,
    data: Map<String, Any?>
): RideReview? {
    val driverId = readString(data, "driverId").orEmpty()
    val passengerId = readString(data, "passengerId").orEmpty()
    if (driverId.isBlank() || passengerId.isBlank()) {
        return null
    }

    return RideReview(
        reviewId = readString(data, "reviewId").orEmpty().ifBlank { reviewId },
        rideId = readString(data, "rideId").orEmpty(),
        driverId = driverId,
        driverName = readString(data, "driverName").orEmpty(),
        passengerId = passengerId,
        passengerName = readString(data, "passengerName").orEmpty(),
        stars = readInt(data, "stars")?.coerceIn(0, 5) ?: 0,
        comment = readString(data, "comment").orEmpty(),
        createdAt = readInstant(data, "createdAt")
    )
}

internal fun buildReviewId(driverId: String, passengerId: String): String {
    return "${driverId}_${passengerId}"
}

internal fun readString(data: Map<String, Any?>, field: String): String? {
    return data[field] as? String
}

internal fun readInt(data: Map<String, Any?>, field: String): Int? {
    return when (val value = data[field]) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is Number -> value.toInt()
        else -> null
    }
}

internal fun readInstant(data: Map<String, Any?>, field: String): Instant? {
    return when (val value = data[field]) {
        is Timestamp -> value.toDate().toInstant()
        is Instant -> value
        else -> null
    }
}
