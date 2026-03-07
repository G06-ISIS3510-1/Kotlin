package com.wheels.app.ui.screens.rides

import androidx.lifecycle.ViewModel
import com.wheels.app.domain.usecase.GetAvailableRidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val getAvailableRidesUseCase: GetAvailableRidesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RidesUiState())
    val uiState: StateFlow<RidesUiState> = _uiState.asStateFlow()

    fun onEvent(event: RidesEvent) {
        when (event) {
            RidesEvent.LoadRides -> Unit
        }
    }
}
