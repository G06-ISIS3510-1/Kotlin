package com.wheels.app.features.auth.domain.usecase

import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(request: ForgotPasswordRequest): Resource<Unit> =
        authRepository.forgotPassword(request)
}
