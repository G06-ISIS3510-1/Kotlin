package com.wheels.app.features.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.presentation.viewmodel.ProfileViewModel
import com.wheels.app.features.profile.presentation.viewmodel.ProfileEvent

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    viewModel: ProfileViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        // Header + Profile Card (overlapped)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1a3a5c),
                                    Color(0xFF2d5280)
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 5.dp)
                ) {
                    Column {
                        Text(
                            text = "Profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Manage your account and preferences",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 126.dp)
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF1a3a5c).copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorScheme.surface)
                        .padding(24.dp)
                ) {
                    Column {
                        // Avatar and Name Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Get initials from name
                            val initials = state.name.split(" ")
                                .take(2)
                                .map { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF5b89c8),
                                                Color(0xFF1a3a5c)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = state.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shield,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF00d9a3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Verified Student",
                                        fontSize = 12.sp,
                                        color = Color(0xFF00d9a3),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                Text(
                                    text = state.memberSinceLabel,
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RoleSwitchButton(
                                        label = "Passenger",
                                        selected = state.activeRole == UserRole.PASSENGER,
                                        enabled = UserRole.PASSENGER in state.availableRoles,
                                        onClick = {
                                            viewModel.onEvent(ProfileEvent.RoleChanged(UserRole.PASSENGER))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    RoleSwitchButton(
                                        label = "Driver",
                                        selected = state.activeRole == UserRole.DRIVER,
                                        enabled = UserRole.DRIVER in state.availableRoles,
                                        onClick = {
                                            viewModel.onEvent(ProfileEvent.RoleChanged(UserRole.DRIVER))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                val roleInfoMessage = state.roleInfoMessage
                                if (roleInfoMessage != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = roleInfoMessage,
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                val roleUpgradeTarget = state.roleUpgradeTarget
                                if (roleUpgradeTarget != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colorScheme.surfaceVariant)
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Enable ${roleUpgradeTarget.displayName} role",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Confirm your password and we'll add this role to your account.",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        OutlinedTextField(
                                            value = state.roleUpgradePassword,
                                            onValueChange = {
                                                viewModel.onEvent(ProfileEvent.RoleUpgradePasswordChanged(it))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Password") },
                                            visualTransformation = PasswordVisualTransformation(),
                                            singleLine = true
                                        )
                                        val roleUpgradeErrorMessage = state.roleUpgradeErrorMessage
                                        if (roleUpgradeErrorMessage != null) {
                                            Text(
                                                text = roleUpgradeErrorMessage,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.onEvent(ProfileEvent.DismissRoleUpgradePrompt)
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Cancel")
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.onEvent(ProfileEvent.ConfirmRoleUpgrade)
                                                },
                                                enabled = state.roleUpgradePassword.isNotBlank() && !state.roleActionLoading,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (state.roleActionLoading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Text("Enable Role")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Stats Grid
                        val trustScoreValue = when {
                            state.activeRole != UserRole.DRIVER -> "--"
                            state.trustScoreLoading -> "..."
                            state.trustScore != null -> "${state.trustScore}%"
                            else -> "--"
                        }

                        val stats = listOf(
                            Triple("${state.ridesCount}", "Rides", Color(0xFF1a3a5c)),
                            Triple(
                                trustScoreValue,
                                if (state.activeRole == UserRole.DRIVER) "Driver Trust" else "Trust",
                                if (state.activeRole == UserRole.DRIVER) Color(0xFF00d9a3) else Color(0xFF94A3B8)
                            ),
                            Triple("5.0", "Rating", Color(0xFFffa726)),
                            Triple("142", "Points", Color(0xFF5b89c8))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            stats.forEach { (value, label, color) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = value,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Contact Information Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Contact Information",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0xFF1a3a5c).copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Email
                        ContactInfoRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = state.email
                        )

                        Divider(
                            color = colorScheme.outline,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone
                        ContactInfoRow(
                            icon = Icons.Outlined.Phone,
                            label = "Phone",
                            value = state.phone.ifBlank { "Not provided yet" }
                        )
                    }
                }
            }
        }

        // Account Settings Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(
                    text = "Account",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0xFF1a3a5c).copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Column {
                        MenuItemRow(
                            icon = Icons.Outlined.StarBorder,
                            title = "Trust & Fairness",
                            subtitle = "View your reliability metrics",
                            onClick = { navController.navigate(Destinations.TrustFairness.route) },
                            showDivider = true
                        )
                        MenuItemRow(
                            icon = Icons.Outlined.CreditCard,
                            title = "Payment Methods",
                            subtitle = "Manage your payment options",
                            showDivider = true
                        )
                        MenuItemRow(
                            icon = Icons.Outlined.CardGiftcard,
                            title = "Rewards & Points",
                            subtitle = "Redeem your 142 points",
                            showDivider = false
                        )
                    }
                }
            }
        }

        // Settings Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Settings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0xFF1a3a5c).copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Column {
                        MenuItemRow(
                            icon = Icons.Outlined.Palette,
                            title = "UI Theme",
                            subtitle = "Adjust dark mode and adaptive appearance",
                            onClick = { navController.navigate(Destinations.UiTheme.route) },
                            showDivider = true
                        )
                        MenuItemRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications",
                            subtitle = "Manage notification preferences",
                            showDivider = true
                        )
                        MenuItemRow(
                            icon = Icons.Outlined.Shield,
                            title = "Privacy & Security",
                            subtitle = "Control your privacy settings",
                            showDivider = true
                        )
                        MenuItemRow(
                            icon = Icons.Outlined.Info,
                            title = "Help & Support",
                            subtitle = "Get help and contact support",
                            showDivider = false
                        )
                    }
                }
            }
        }

        // Logout Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0xFF1a3a5c).copy(alpha = 0.08f)
                    )
                    .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                    .background(colorScheme.surface, RoundedCornerShape(20.dp))
                    .clickable { viewModel.onEvent(ProfileEvent.LogOut) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFff5252),
                        modifier = Modifier.padding(end = 8.dp).size(20.dp)
                    )
                    Text(
                        text = "Log Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFff5252)
                    )
                }
            }
        }

        // Bottom spacer
        item {
            Box(modifier = Modifier.height(80.dp))
        }
        }
    }
}

@Composable
private fun RoleSwitchButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) colorScheme.primary else if (enabled) colorScheme.surfaceVariant else colorScheme.outline.copy(alpha = 0.2f)
            )
            .border(
                width = 1.5.dp,
                color = if (selected) colorScheme.primary else colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colorScheme.onPrimary else if (enabled) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun ContactInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Navigate",
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    showDivider: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            Divider(
                color = colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
