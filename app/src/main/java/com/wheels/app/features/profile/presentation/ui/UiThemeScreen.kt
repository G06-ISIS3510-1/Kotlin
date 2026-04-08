package com.wheels.app.features.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun UiThemeScreen(
    innerPadding: PaddingValues,
    navController: NavController
) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var adaptiveThemeEnabled by remember { mutableStateOf(false) }
    var savedDarkModePreference by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1a3a5c),
                                    Color(0xFF2d5280)
                                )
                            )
                        )
                        .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .clickable { navController.popBackStack() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "UI Theme",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Dark mode can reduce bright blues and may feel better for vision under low light conditions.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ThemeSwitchRow(
                            icon = if (darkModeEnabled) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            title = "Dark mode",
                            subtitle = if (darkModeEnabled) "Enabled" else "Disabled",
                            checked = darkModeEnabled,
                            enabled = !adaptiveThemeEnabled,
                            onCheckedChange = { checked ->
                                if (!adaptiveThemeEnabled) {
                                    darkModeEnabled = checked
                                    savedDarkModePreference = checked
                                }
                            }
                        )

                        Divider(
                            color = Color(0xFFe5e9f2),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ThemeSwitchRow(
                            icon = Icons.Outlined.WbSunny,
                            title = "Adaptive UI Theme",
                            subtitle = "The UI Theme adapts to the environmental light.",
                            checked = adaptiveThemeEnabled,
                            enabled = true,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    savedDarkModePreference = darkModeEnabled
                                    darkModeEnabled = false
                                } else {
                                    darkModeEnabled = savedDarkModePreference
                                }
                                adaptiveThemeEnabled = checked
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (enabled) Color(0xFFe8f0f9) else Color(0xFFEEF2F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) Color(0xFF5b89c8) else Color(0xFF9AA9BC),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color(0xFF1a3a5c) else Color(0xFF8FA1B6)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (enabled) Color(0xFF64748b) else Color(0xFFA0AEC0)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF5b89c8),
                uncheckedTrackColor = Color(0xFFD7E0EC),
                uncheckedBorderColor = Color(0xFFD7E0EC),
                disabledCheckedThumbColor = Color(0xFFE2E8F0),
                disabledUncheckedThumbColor = Color(0xFFE2E8F0),
                disabledCheckedTrackColor = Color(0xFFC8D3E2),
                disabledUncheckedTrackColor = Color(0xFFDEE6F0),
                disabledUncheckedBorderColor = Color(0xFFDEE6F0),
                disabledCheckedBorderColor = Color(0xFFC8D3E2)
            )
        )
    }
}
