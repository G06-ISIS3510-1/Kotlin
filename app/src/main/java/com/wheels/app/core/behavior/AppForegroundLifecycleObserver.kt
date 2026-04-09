package com.wheels.app.core.behavior

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.wheels.app.core.behavior.domain.event.AppOpenSource
import com.wheels.app.core.behavior.domain.model.AppOpenIdentity
import com.wheels.app.core.behavior.domain.usecase.TrackAppOpenUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class AppForegroundLifecycleObserver @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val trackAppOpenUseCase: TrackAppOpenUseCase
) : DefaultLifecycleObserver {

    private val observerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStart(owner: LifecycleOwner) {
        // Foreground opens are only tracked for signed-in users, so every event
        // can be tied to Firestore usage data and the user's FCM tokens later.
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email.orEmpty()
        if (email.isBlank()) return

        observerScope.launch {
            trackAppOpenUseCase(
                identity = AppOpenIdentity(
                    uid = currentUser.uid,
                    email = email
                ),
                source = AppOpenSource.FOREGROUND
            )
        }
    }
}
