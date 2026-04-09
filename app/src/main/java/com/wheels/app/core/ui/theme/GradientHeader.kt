package com.wheels.app.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val GradientHeaderStart = Color(0xFF1A3A5C)
private val GradientHeaderEnd = Color(0xFF2D5280)

val GradientHeaderPrimaryContent = Color.White
val GradientHeaderSecondaryContent = Color.White.copy(alpha = 0.84f)
val GradientHeaderIconContainer = Color.White.copy(alpha = 0.12f)

@Composable
fun gradientHeaderBrush(): Brush = Brush.linearGradient(
    colors = listOf(GradientHeaderStart, GradientHeaderEnd)
)
