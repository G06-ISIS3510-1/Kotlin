package com.wheels.app.core.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SessionTransitionCoordinator @Inject constructor() {

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    fun beginSignOut() {
        _isSigningOut.value = true
    }

    fun completeSignOut() {
        _isSigningOut.value = false
    }
}
