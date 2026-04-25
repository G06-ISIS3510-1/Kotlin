package com.wheels.app.features.auth.data.remote.mapper

import com.wheels.app.features.auth.data.remote.dto.CreateAccountRequestDto
import com.wheels.app.features.auth.data.remote.dto.ForgotPasswordRequestDto
import com.wheels.app.features.auth.data.remote.dto.SignInRequestDto
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest

/**
 * Mapea domain models a DTOs de request para la API.
 * Responsabilidad: Domain → Data (Remote)
 */

fun CreateAccountRequest.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    fullName = fullName,
    email = username,
    password = password,
    phone = phone,
    isDriver = roles.contains(com.wheels.app.core.session.UserRole.DRIVER)
)

fun SignInRequest.toDto(): SignInRequestDto = SignInRequestDto(
    email = username,
    password = password
)

fun ForgotPasswordRequest.toDto(): ForgotPasswordRequestDto = ForgotPasswordRequestDto(
    email = username
)
