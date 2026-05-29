package com.wheels.app.features.reviews.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.reviews.data.local.PendingRideReviewEntity
import com.wheels.app.features.reviews.data.local.PendingRideReviewLocalStore
import com.wheels.app.features.reviews.data.local.toRideReview
import com.wheels.app.features.reviews.data.repository.buildReviewDocumentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Replays queued reviews to Firestore after connectivity returns.
 *
 * The worker runs off the main thread, writes remote first, and only deletes local rows after
 * Firestore confirms the write.
 */
class RideReviewSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Background replay is IO-bound:
        // read the local queue, publish to Firestore, and retry later if anything fails.
        runCatching {
            FirebaseApp.initializeApp(applicationContext)

            val queueStore = PendingRideReviewLocalStore(applicationContext)
            val firestore = FirebaseFirestore.getInstance()

            while (true) {
                // Drain the queue fully so items added while we were running still get picked up.
                val pendingReviews = queueStore.getPendingRideReviews()
                if (pendingReviews.isEmpty()) break

                pendingReviews.forEach { pendingReview ->
                    syncPendingReview(
                        firestore = firestore,
                        queueStore = queueStore,
                        pendingReview = pendingReview
                    )
                }
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    private suspend fun syncPendingReview(
        firestore: FirebaseFirestore,
        queueStore: PendingRideReviewLocalStore,
        pendingReview: PendingRideReviewEntity
    ) {
        // Remote write first, local delete second.
        // That keeps the review durable if the network drops mid-flight.
        val review = pendingReview.toRideReview()
        Tasks.await(
            firestore.collection(REVIEWS_COLLECTION)
                .document(review.reviewId)
                .set(buildReviewDocumentData(review))
        )
        queueStore.deletePendingRideReview(pendingReview.reviewKey)
    }

    companion object {
        const val WORK_NAME = "ride_review_sync"
        private const val REVIEWS_COLLECTION = "reviews"
    }
}
