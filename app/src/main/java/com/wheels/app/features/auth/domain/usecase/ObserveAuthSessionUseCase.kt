package com.wheels.app.features.auth.domain.usecase

import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> = authRepository.observeAuthSession()
}
