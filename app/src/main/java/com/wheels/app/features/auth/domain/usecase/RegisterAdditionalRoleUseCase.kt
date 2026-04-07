package com.wheels.app.features.auth.domain.usecase

import com.wheels.app.core.common.Resource
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterAdditionalRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(role: UserRole, password: String): Resource<AuthUser> {
        return authRepository.registerAdditionalRole(role, password)
    }
}
