package com.wheels.app.features.payments.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentsEvent) {
        when (event) {
            PaymentsEvent.LoadPayments -> Unit
        }
    }
}

sealed interface PaymentsEvent {
    data object LoadPayments : PaymentsEvent
}

data class PaymentsUiState(
    val isLoading: Boolean = false,
    val pendingAmount: Double = 0.0
)
