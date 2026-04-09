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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wheels.app.core.ui.theme.GradientHeaderPrimaryContent
import com.wheels.app.core.ui.theme.GradientHeaderSecondaryContent
import com.wheels.app.core.theme.ThemeSettingsViewModel
import com.wheels.app.core.ui.theme.gradientHeaderBrush

@Composable
fun UiThemeScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: ThemeSettingsViewModel
) {
    val themeState = viewModel.uiState.collectAsStateWithLifecycle()
    val darkModeEnabled = themeState.value.isDarkModeEnabled
    val adaptiveThemeEnabled = themeState.value.isAdaptiveThemeEnabled
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                            brush = gradientHeaderBrush()
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
                                tint = GradientHeaderPrimaryContent,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.bodySmall,
                                color = GradientHeaderPrimaryContent,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "UI Theme",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientHeaderPrimaryContent,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Dark mode can reduce bright blues and may feel better for vision under low light conditions.",
                            fontSize = 12.sp,
                            color = GradientHeaderSecondaryContent
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
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ThemeSwitchRow(
                            icon = if (darkModeEnabled) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            title = "Dark mode",
                            subtitle = if (darkModeEnabled) "Enabled" else "Disabled",
                            checked = darkModeEnabled,
                            enabled = !adaptiveThemeEnabled,
                            onCheckedChange = viewModel::setDarkModeEnabled
                        )

                        Divider(
                            color = colorScheme.outline,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ThemeSwitchRow(
                            icon = Icons.Outlined.WbSunny,
                            title = "Adaptive UI Theme",
                            subtitle = "The UI Theme adapts to the environmental light.",
                            checked = adaptiveThemeEnabled,
                            enabled = true,
                            onCheckedChange = viewModel::setAdaptiveThemeEnabled
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
    val colorScheme = MaterialTheme.colorScheme

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
                .background(if (enabled) colorScheme.surfaceVariant else colorScheme.outline.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
                color = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (enabled) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.onPrimary,
                uncheckedThumbColor = colorScheme.surface,
                checkedTrackColor = colorScheme.primary,
                uncheckedTrackColor = colorScheme.outline.copy(alpha = 0.35f),
                uncheckedBorderColor = colorScheme.outline,
                disabledCheckedThumbColor = colorScheme.surface,
                disabledUncheckedThumbColor = colorScheme.surface,
                disabledCheckedTrackColor = colorScheme.outline.copy(alpha = 0.5f),
                disabledUncheckedTrackColor = colorScheme.outline.copy(alpha = 0.25f),
                disabledUncheckedBorderColor = colorScheme.outline.copy(alpha = 0.7f),
                disabledCheckedBorderColor = colorScheme.outline.copy(alpha = 0.7f)
            )
        )
    }
}
