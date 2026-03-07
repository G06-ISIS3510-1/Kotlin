package com.wheels.app.ui.screens.payments

sealed interface PaymentsEvent {
    data object LoadPayments : PaymentsEvent
}
