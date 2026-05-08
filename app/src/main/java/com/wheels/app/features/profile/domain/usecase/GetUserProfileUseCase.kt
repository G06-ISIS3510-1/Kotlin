package com.wheels.app.features.profile.domain.usecase

import com.wheels.app.features.profile.domain.model.User
import com.wheels.app.features.profile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(): Flow<User?> = userProfileRepository.observeCurrentUserProfile()
}
