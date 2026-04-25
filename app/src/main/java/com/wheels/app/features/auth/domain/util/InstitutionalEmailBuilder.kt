package com.wheels.app.features.auth.domain.util

private const val INSTITUTIONAL_DOMAIN = "uniandes.edu.co"

fun buildInstitutionalEmail(usernameOrEmail: String): String {
    val normalized = usernameOrEmail.trim().lowercase()
    val username = normalized.substringBefore("@")
    return "$username@$INSTITUTIONAL_DOMAIN"
}
