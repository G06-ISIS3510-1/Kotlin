package com.wheels.app.data.repository

import com.wheels.app.domain.model.Payment
import com.wheels.app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor() : PaymentRepository {
    override fun getPayments(): Flow<List<Payment>> = flowOf(
        listOf(
            Payment(
                id = "p_001",
                bookingId = "b_001",
                amount = 8000.0,
                currency = "COP",
                status = "HELD_IN_ESCROW"
            )
        )
    )
}
