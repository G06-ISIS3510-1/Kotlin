package com.wheels.app.features.payments.data.remote.mapper

import com.wheels.app.features.payments.data.remote.dto.PaymentDto
import com.wheels.app.features.payments.domain.model.Payment

fun PaymentDto.toDomain(): Payment = Payment(
    id = id,
    bookingId = bookingId,
    amount = amount,
    currency = currency,
    status = status
)
