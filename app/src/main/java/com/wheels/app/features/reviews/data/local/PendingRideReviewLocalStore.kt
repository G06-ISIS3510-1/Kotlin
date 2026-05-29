package com.wheels.app.features.reviews.data.local

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONObject

private const val PENDING_REVIEW_STORE_NAME = "pending_ride_reviews.preferences_pb"
private const val PENDING_REVIEW_PREFIX = "pending_ride_review_"

// DataStore is enough here because the offline queue is small and simple.
// We do not need a full relational table just to hold pending reviews until they sync.
private val Context.pendingRideReviewDataStore by preferencesDataStore(
    name = PENDING_REVIEW_STORE_NAME
)

/**
 * Small local queue for reviews that are waiting to be published.
 *
 * DataStore keeps this lightweight and durable without introducing a larger database schema.
 */
@Singleton
class PendingRideReviewLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.pendingRideReviewDataStore

    fun observePendingRideReview(reviewKey: String): Flow<PendingRideReviewEntity?> {
        if (reviewKey.isBlank()) return flowOf(null)

        // Reuse the full queue flow so the single-item observer and the list observer stay in sync.
        return observePendingRideReviews()
            .map { reviews -> reviews.firstOrNull { it.reviewKey == reviewKey } }
    }

    fun observePendingRideReviews(): Flow<List<PendingRideReviewEntity>> {
        // Expose the whole queue so the Home screen and sync worker can share the same source of truth.
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences -> preferences.toPendingRideReviewEntities() }
            .distinctUntilChanged()
    }

    suspend fun getPendingRideReviews(): List<PendingRideReviewEntity> {
        // The sync worker uses a snapshot read here, then deletes items one by one after Firestore succeeds.
        val preferences = dataStore.data
            .catch { exception -> 
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        return preferences.toPendingRideReviewEntities()
    }

    suspend fun upsertPendingRideReview(review: PendingRideReviewEntity) {
        // Upsert lets us replace the same ride/passenger review if submit is retried.
        dataStore.edit { preferences ->
            preferences[entryKey(review.reviewKey)] = review.toStoredJson()
        }
    }

    suspend fun deletePendingRideReview(reviewKey: String) {
        if (reviewKey.isBlank()) return

        // Delete only after the remote publish succeeds.
        dataStore.edit { preferences ->
            preferences.remove(entryKey(reviewKey))
        }
    }

    private fun entryKey(reviewKey: String) = stringPreferencesKey(
        "$PENDING_REVIEW_PREFIX${encodeReviewKey(reviewKey)}"
    )

    private fun encodeReviewKey(reviewKey: String): String {
        // Encode the composite key because DataStore preference keys must be strings.
        return Base64.encodeToString(
            reviewKey.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE
        )
    }

    private fun PendingRideReviewEntity.toStoredJson(): String {
        // Store as JSON so the queue format stays easy to inspect and evolve.
        return JSONObject().apply {
            put("reviewKey", reviewKey)
            put("rideId", rideId)
            put("driverId", driverId)
            put("driverName", driverName)
            put("passengerId", passengerId)
            put("passengerName", passengerName)
            put("stars", stars)
            put("comment", comment)
            put("createdAtMillis", createdAtMillis)
        }.toString()
    }

    private fun Preferences.toPendingRideReviewEntities(): List<PendingRideReviewEntity> {
        // Read every pending review back out of preferences and keep the queue ordered by creation time.
        return asMap()
            .entries
            .mapNotNull { (key, value) ->
                if (!key.name.startsWith(PENDING_REVIEW_PREFIX) || value !is String) {
                    return@mapNotNull null
                }

                value.toPendingRideReviewEntityOrNull()
            }
            .sortedBy { it.createdAtMillis }
    }

    private fun String?.toPendingRideReviewEntityOrNull(): PendingRideReviewEntity? {
        val raw = this ?: return null

        return runCatching {
            // If one record is malformed, skip it instead of breaking the entire queue.
            val json = JSONObject(raw)
            PendingRideReviewEntity(
                reviewKey = json.optString("reviewKey"),
                rideId = json.optString("rideId"),
                driverId = json.optString("driverId"),
                driverName = json.optString("driverName"),
                passengerId = json.optString("passengerId"),
                passengerName = json.optString("passengerName"),
                stars = json.optInt("stars").coerceIn(0, 5),
                comment = json.optString("comment").trim(),
                createdAtMillis = json.optLong("createdAtMillis")
            ).takeIf {
                it.reviewKey.isNotBlank() &&
                    it.rideId.isNotBlank() &&
                    it.passengerId.isNotBlank()
            }
        }.getOrNull()
    }
}
