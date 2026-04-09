package com.wheels.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.wheels.app.core.behavior.AppForegroundLifecycleObserver
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WheelsApplication : Application() {

    @Inject
    lateinit var appForegroundLifecycleObserver: AppForegroundLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appForegroundLifecycleObserver)
    }
}
