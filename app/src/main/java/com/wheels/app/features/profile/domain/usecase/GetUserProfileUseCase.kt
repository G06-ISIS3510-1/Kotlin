package com.wheels.app.features.profile.domain.usecase

import com.wheels.app.features.profile.domain.model.User
import com.wheels.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.getCurrentUser()
}
