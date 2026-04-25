package com.wheels.app.core.analytics.bq13.domain.repository

import com.wheels.app.core.analytics.bq13.domain.model.BQ13Result

interface BQ13Repository {
    suspend fun getFrequentDestinations(userId: String): BQ13Result?
}
