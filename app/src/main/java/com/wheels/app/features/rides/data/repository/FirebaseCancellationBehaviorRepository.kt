package com.wheels.app.features.rides.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.rides.domain.model.CancellationBehaviorMetrics
import com.wheels.app.features.rides.domain.repository.CancellationBehaviorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCancellationBehaviorRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : CancellationBehaviorRepository {

    override fun observeCancellationBehaviorMetrics(userId: String): Flow<CancellationBehaviorMetrics?> {
        return callbackFlow {
            val registration = firestore
                .collection(USER_CANCELLATION_METRICS_COLLECTION)
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val metrics = if (snapshot == null || !snapshot.exists()) {
                        null
                    } else {
                        CancellationBehaviorMetrics(
                            userId = userId,
                            cancellationCount = snapshot.getLong("cancellationCount")?.toInt() ?: 0,
                            averageHoursBeforeCancellation = snapshot.getDouble("averageHoursBeforeCancellation") ?: 0.0
                        )
                    }

                    trySend(metrics)
                }

            awaitClose { registration.remove() }
        }.flowOn(ioDispatcher)
    }

    private companion object {
        const val USER_CANCELLATION_METRICS_COLLECTION = "user_cancellation_metrics"
    }
}
