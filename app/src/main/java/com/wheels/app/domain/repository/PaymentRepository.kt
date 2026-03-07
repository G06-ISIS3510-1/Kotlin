package com.wheels.app.domain.repository

import com.wheels.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getPayments(): Flow<List<Payment>>
}
