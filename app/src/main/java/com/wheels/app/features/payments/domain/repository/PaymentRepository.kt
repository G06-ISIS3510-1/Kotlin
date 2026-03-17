package com.wheels.app.features.payments.domain.repository

import com.wheels.app.features.payments.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getPayments(): Flow<List<Payment>>
}
