package com.wheels.app.features.auth.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.wheels.app.core.ui.theme.ElectricGreen

@Composable
fun AuthBrandLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(32.dp)) {
        drawCircle(
            color = ElectricGreen,
            radius = size.minDimension * 0.46f,
            style = Stroke(width = 2.5.dp.toPx())
        )
        val start = size.minDimension * 0.28f
        val end = size.minDimension * 0.72f
        val center = size.minDimension / 2f
        drawLine(
            color = ElectricGreen,
            start = Offset(start, center),
            end = Offset(end, center),
            strokeWidth = 2.5.dp.toPx()
        )
        drawLine(
            color = ElectricGreen,
            start = Offset(center, start),
            end = Offset(center, end),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}
