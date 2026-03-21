package com.wheels.app.core.common

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
}
