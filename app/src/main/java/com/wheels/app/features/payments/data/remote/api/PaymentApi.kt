package com.wheels.app.features.payments.data.remote.api

import com.wheels.app.features.payments.data.remote.dto.PaymentDto
import retrofit2.http.GET

interface PaymentApi {
    @GET("payments")
    suspend fun getPayments(): List<PaymentDto>
}
