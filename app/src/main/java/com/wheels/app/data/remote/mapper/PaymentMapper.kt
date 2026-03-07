package com.wheels.app.data.remote.mapper

import com.wheels.app.data.remote.dto.PaymentDto
import com.wheels.app.domain.model.Payment

fun PaymentDto.toDomain(): Payment = Payment(
    id = id,
    bookingId = bookingId,
    amount = amount,
    currency = currency,
    status = status
)
