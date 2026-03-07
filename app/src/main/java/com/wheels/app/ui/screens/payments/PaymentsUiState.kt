package com.wheels.app.ui.screens.payments

data class PaymentsUiState(
    val isLoading: Boolean = false,
    val pendingAmount: Double = 0.0
)
