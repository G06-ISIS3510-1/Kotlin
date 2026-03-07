package com.wheels.app.common

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
}
