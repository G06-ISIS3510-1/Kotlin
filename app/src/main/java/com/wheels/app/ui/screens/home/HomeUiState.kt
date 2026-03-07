package com.wheels.app.ui.screens.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val welcomeTitle: String = "Bienvenido a Wheels",
    val welcomeSubtitle: String = "Busca, reserva y gestiona tus rides universitarios"
)
