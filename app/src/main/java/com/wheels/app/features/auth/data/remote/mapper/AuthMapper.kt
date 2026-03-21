package com.wheels.app.features.auth.data.remote.mapper

import com.wheels.app.features.auth.data.remote.dto.CreateAccountRequestDto
import com.wheels.app.features.auth.data.remote.dto.SignInRequestDto
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.SignInRequest

/**
 * Mapea domain models a DTOs de request para la API.
 * Responsabilidad: Domain → Data (Remote)
 */

fun CreateAccountRequest.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    fullName = fullName,
    email = email,
    password = password,
    phone = phone,
    isDriver = role.name == "DRIVER"
)

fun SignInRequest.toDto(): SignInRequestDto = SignInRequestDto(
    fullName = fullName,
    email = email,
    password = password
)
