package com.wheels.app.core.analytics.bq13.domain.connectivity

interface ConnectivityChecker {
    fun isConnected(): Boolean
}
