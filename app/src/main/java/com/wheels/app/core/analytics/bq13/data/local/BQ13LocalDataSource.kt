package com.wheels.app.core.analytics.bq13.data.local

import android.content.Context
import com.wheels.app.core.analytics.bq13.domain.model.BQ13_CACHE_VERSION
import com.wheels.app.core.analytics.bq13.domain.model.BQ13DataSource
import com.wheels.app.core.analytics.bq13.domain.model.BQ13FrequentDestination
import com.wheels.app.core.analytics.bq13.domain.model.BQ13Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BQ13LocalDataSource @Inject constructor(
    @ApplicationContext context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val sharedPreferences = context.getSharedPreferences(
        BQ13_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    suspend fun getCachedResult(userId: String): BQ13Result? = withContext(ioDispatcher) {
        val rawJson = sharedPreferences.getString(cacheKey(userId), null) ?: return@withContext null
        runCatching { rawJson.toBQ13Result() }.getOrNull()
            ?.takeIf { it.cacheVersion == BQ13_CACHE_VERSION }
            ?.copy(source = BQ13DataSource.CACHE)
    }

    suspend fun saveResult(result: BQ13Result) = withContext(ioDispatcher) {
        sharedPreferences.edit()
            .putString(cacheKey(result.userId), result.toJson().toString())
            .apply()
    }

    private fun BQ13Result.toJson(): JSONObject {
        return JSONObject()
            .put("userId", userId)
            .put("lastUpdatedMillis", lastUpdatedMillis)
            .put("cacheVersion", BQ13_CACHE_VERSION)
            .put(
                "destinations",
                JSONArray().also { array ->
                    destinations.forEach { destination ->
                        array.put(
                            JSONObject()
                                .put("userId", destination.userId)
                                .put("destinationName", destination.destinationName)
                                .put("latitude", destination.latitude)
                                .put("longitude", destination.longitude)
                                .put("count", destination.count)
                                .put("lastUpdatedMillis", destination.lastUpdatedMillis)
                                .put("cacheVersion", BQ13_CACHE_VERSION)
                        )
                    }
                }
            )
    }

    private fun String.toBQ13Result(): BQ13Result {
        val json = JSONObject(this)
        val userId = json.getString("userId")
        val lastUpdatedMillis = json.getLong("lastUpdatedMillis")
        val cacheVersion = json.optString("cacheVersion", "")
        val destinationsJson = json.optJSONArray("destinations") ?: JSONArray()

        val destinations = buildList {
            for (index in 0 until destinationsJson.length()) {
                val item = destinationsJson.getJSONObject(index)
                add(
                    BQ13FrequentDestination(
                        userId = item.optString("userId", userId),
                        destinationName = item.getString("destinationName"),
                        latitude = item.optNullableDouble("latitude"),
                        longitude = item.optNullableDouble("longitude"),
                        count = item.getInt("count"),
                        lastUpdatedMillis = item.optLong("lastUpdatedMillis", lastUpdatedMillis),
                        cacheVersion = item.optString("cacheVersion", BQ13_CACHE_VERSION)
                    )
                )
            }
        }

        return BQ13Result(
            userId = userId,
            destinations = destinations,
            lastUpdatedMillis = lastUpdatedMillis,
            cacheVersion = cacheVersion,
            source = BQ13DataSource.CACHE
        )
    }

    private fun JSONObject.optNullableDouble(name: String): Double? {
        return if (isNull(name)) null else optDouble(name)
    }

    private fun cacheKey(userId: String): String = "${BQ13_CACHE_VERSION}_$userId"

    private companion object {
        const val BQ13_PREFERENCES_NAME = "bq13_frequent_destinations"
    }
}
