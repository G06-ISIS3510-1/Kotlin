package com.wheels.app.features.reviews.data.local

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMemoryReviewsCacheSizer @Inject constructor(
    @ApplicationContext context: Context
) : ReviewsCacheSizer {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override fun maxEntries(): Int {
        return calculateReviewsCacheMaxEntries(
            memoryClassMb = activityManager.memoryClass,
            isLowRamDevice = activityManager.isLowRamDevice
        )
    }
}
