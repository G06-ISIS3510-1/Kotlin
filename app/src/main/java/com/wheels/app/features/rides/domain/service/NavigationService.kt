package com.wheels.app.features.rides.domain.service

import android.content.Context
import com.wheels.app.features.rides.domain.model.RouteDestination

/**
 * Note: keeping Context here is a pragmatic compromise for this project because
 * launching external navigation apps depends on Android intents.
 */
interface NavigationService {
    fun openRoute(
        context: Context,
        destination: RouteDestination,
        origin: String? = null
    ): Boolean

    fun isAvailable(context: Context): Boolean
}
