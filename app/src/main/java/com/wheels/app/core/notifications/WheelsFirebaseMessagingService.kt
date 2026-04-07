package com.wheels.app.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wheels.app.core.session.domain.usecase.UpdateCurrentUserFcmTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WheelsFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var updateCurrentUserFcmTokenUseCase: UpdateCurrentUserFcmTokenUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            runCatching { updateCurrentUserFcmTokenUseCase(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
