package com.wheels.app.data.remote.api

import com.wheels.app.data.remote.dto.PaymentDto
import retrofit2.http.GET

interface PaymentApi {
    @GET("payments")
    suspend fun getPayments(): List<PaymentDto>
}
