package com.wheels.app.features.auth.domain.usecase

import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RestoreSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? = authRepository.restoreSession()
}
