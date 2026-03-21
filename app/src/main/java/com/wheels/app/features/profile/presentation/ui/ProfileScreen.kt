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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
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
                        .background(Color.White)
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
                                    color = Color(0xFF1a3a5c),
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
                                    text = "Member since Jan 2025",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748b)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RoleSwitchButton(
                                        label = "Passenger",
                                        selected = state.activeRole == UserRole.PASSENGER,
                                        onClick = {
                                            viewModel.onEvent(ProfileEvent.RoleChanged(UserRole.PASSENGER))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    RoleSwitchButton(
                                        label = "Driver",
                                        selected = state.activeRole == UserRole.DRIVER,
                                        onClick = {
                                            viewModel.onEvent(ProfileEvent.RoleChanged(UserRole.DRIVER))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Stats Grid
                        val trustScoreValue = when {
                            state.trustScoreLoading -> "..."
                            state.trustScore != null -> "${state.trustScore}%"
                            else -> "--"
                        }

                        val stats = listOf(
                            Triple("${state.ridesCount}", "Rides", Color(0xFF1a3a5c)),
                            Triple(trustScoreValue, "Trust", Color(0xFF00d9a3)),
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
                                        .background(Color(0xFFF7F9FC))
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
                                            color = Color(0xFF64748b)
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
                    color = Color(0xFF64748b),
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
                        .background(Color.White)
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
                            color = Color(0xFFe5e9f2),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone
                        ContactInfoRow(
                            icon = Icons.Outlined.Phone,
                            label = "Phone",
                            value = state.phone
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
                    color = Color(0xFF64748b),
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
                        .background(Color.White)
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Trust & Fairness Theme",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1a3a5c)
                                )
                                Text(
                                    text = if (state.trustFairnessDarkMode) "Dark mode enabled" else "Light mode enabled",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748b)
                                )
                            }
                            TextButton(
                                onClick = { viewModel.onEvent(ProfileEvent.ToggleTrustFairnessDarkMode) }
                            ) {
                                Text(
                                    text = if (state.trustFairnessDarkMode) "Use Light" else "Use Dark",
                                    color = Color(0xFF1a3a5c),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Divider(
                            color = Color(0xFFe5e9f2),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
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
                    color = Color(0xFF64748b),
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
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Column {
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
                    .border(2.dp, Color(0xFFe5e9f2), RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable { /* Handle logout */ }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF1a3a5c) else Color(0xFFF7F9FC))
            .border(
                width = 1.5.dp,
                color = if (selected) Color(0xFF1a3a5c) else Color(0xFFE2E8F0),
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
            color = if (selected) Color.White else Color(0xFF64748b)
        )
    }
}

@Composable
fun ContactInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFe8f0f9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF5b89c8),
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
                color = Color(0xFF64748b)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1a3a5c)
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xFF64748b),
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
                    .background(Color(0xFFe8f0f9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF5b89c8),
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
                    color = Color(0xFF1a3a5c)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748b)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF64748b),
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            Divider(
                color = Color(0xFFe5e9f2),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
