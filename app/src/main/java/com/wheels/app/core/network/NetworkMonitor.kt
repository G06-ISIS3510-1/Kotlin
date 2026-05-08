package com.wheels.app.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun isOnline(): Boolean
    fun observeIsOnline(): Flow<Boolean>
}
