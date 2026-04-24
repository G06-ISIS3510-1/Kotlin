package com.wheels.app.features.auth.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.WheelsSurface

@Composable
fun WheelsInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, color = Color(0xFF94A3B8))
            },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            leadingIcon = leadingIcon,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = WheelsSurface,
                focusedContainerColor = WheelsSurface,
                unfocusedBorderColor = Color(0xFFE5E9F2),
                focusedBorderColor = Color(0xFF5B89C8),
                cursorColor = PrimaryBlue
            )
        )
    }
}

