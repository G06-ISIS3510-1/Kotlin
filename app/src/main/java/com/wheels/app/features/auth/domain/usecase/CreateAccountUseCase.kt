package com.wheels.app.features.auth.domain.usecase

import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(request: CreateAccountRequest): Resource<AuthUser> =
        authRepository.createAccount(request)
}
