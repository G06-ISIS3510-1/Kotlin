package com.wheels.app.features.auth.domain.model

sealed class AuthFailure(val message: String) {
    data object EmailAlreadyInUse : AuthFailure("That Uniandes email is already registered.")
    data object InvalidEmail : AuthFailure("That university email is not valid.")
    data object WeakPassword : AuthFailure("Choose a stronger password with at least 6 characters.")
    data object InvalidCredentials : AuthFailure("Your username or password is incorrect.")
    data object NetworkError : AuthFailure("Check your internet connection and try again.")
    data class Unknown(val fallbackMessage: String?) : AuthFailure(
        fallbackMessage ?: "Something went wrong while authenticating. Please try again."
    )
}
