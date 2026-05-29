package com.wheels.app.features.reviews.presentation.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.GradientHeaderPrimaryContent
import com.wheels.app.core.ui.theme.GradientHeaderSecondaryContent
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.core.ui.theme.gradientHeaderBrush
import com.wheels.app.features.reviews.presentation.viewmodel.ReviewFeedbackEvent
import com.wheels.app.features.reviews.presentation.viewmodel.ReviewFeedbackViewModel

private const val QUICK_PAY_COMPLETED_KEY = "quick_pay_completed"

/**
 * Review screen UI.
 *
 * It stays simple: collect state from the ViewModel, show the current connection/sync message,
 * and navigate Home once the ViewModel says the flow is complete.
 */
@Composable
fun ReviewFeedbackScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: ReviewFeedbackViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (!state.isComplete) return@LaunchedEffect

        // Save a one-off message in the Home back stack entry, then leave this screen.
        // SavedStateHandle is a lightweight handoff for transient navigation results.
        val notice = state.queuedNotice?.takeIf { it.isNotBlank() }
        val savedStateHandle = runCatching {
            navController.getBackStackEntry(Destinations.Home.route).savedStateHandle
        }.getOrNull() ?: navController.currentBackStackEntry?.savedStateHandle

        savedStateHandle?.set(QUICK_PAY_COMPLETED_KEY, true)
        notice?.let {
            savedStateHandle?.set(
                Destinations.REVIEW_FEEDBACK_QUEUE_NOTICE_KEY,
                it
            )
        }
        // Navigate explicitly so the user never gets stuck on the review screen after submit.
        // This is more reliable than a plain pop when the back stack has shifted in the meantime.
        navController.navigate(Destinations.Home.route) {
            popUpTo(Destinations.Home.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            HeaderSection(
                driverName = state.driverName,
                onBack = { viewModel.onEvent(ReviewFeedbackEvent.Skip) }
            )

            // Prefer the most specific message first:
            // synced confirmation, then offline queued confirmation, then the generic connection banner.
            val bannerMessage = state.syncNotice ?: state.queuedNotice ?: state.connectionNotice
            if (bannerMessage != null) {
                StatusBanner(
                    message = bannerMessage,
                    isSuccess = state.syncNotice != null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DriverCard(driverName = state.driverName)

            Spacer(modifier = Modifier.height(16.dp))

            ReviewPromptCard(
                selectedStars = state.selectedStars,
                comment = state.comment,
                errorMessage = state.errorMessage,
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                onStarSelected = { viewModel.onEvent(ReviewFeedbackEvent.StarSelected(it)) },
                onCommentChanged = { viewModel.onEvent(ReviewFeedbackEvent.CommentChanged(it)) },
                onSubmit = { viewModel.onEvent(ReviewFeedbackEvent.Submit) },
                onSkip = { viewModel.onEvent(ReviewFeedbackEvent.Skip) }
            )
        }
    }
}

@Composable
private fun StatusBanner(
    message: String,
    isSuccess: Boolean
) {
    val containerColor = if (isSuccess) {
        ElectricGreen.copy(alpha = 0.10f)
    } else {
        SecondaryBlue.copy(alpha = 0.10f)
    }
    val borderColor = if (isSuccess) {
        ElectricGreen.copy(alpha = 0.20f)
    } else {
        SecondaryBlue.copy(alpha = 0.20f)
    }
    val contentColor = if (isSuccess) ElectricGreen else SecondaryBlue
    val icon = if (isSuccess) Icons.Outlined.Check else Icons.Outlined.Schedule

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun HeaderSection(
    driverName: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(gradientHeaderBrush())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = GradientHeaderPrimaryContent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Skip",
                color = GradientHeaderSecondaryContent,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rate your ride",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GradientHeaderPrimaryContent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tell us how your trip with $driverName felt. Stars and comments are both optional.",
            style = MaterialTheme.typography.bodyMedium,
            color = GradientHeaderSecondaryContent
        )
    }
}

@Composable
private fun DriverCard(driverName: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "You are rating",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = driverName,
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Leave a star rating, write a comment, or skip if you do not want to add feedback.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReviewPromptCard(
    selectedStars: Int,
    comment: String,
    errorMessage: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onStarSelected: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Stars",
                color = TextSecondary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..5).forEach { star ->
                    val selected = selectedStars >= star
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .background(
                                if (selected) Color(0xFFFFF2D6) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) Color(0xFFFFA726) else Border,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                            .clickable { onStarSelected(star) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (selected) Color(0xFFFFA726) else TextSecondary
                        )
                    }
                }
            }

            Text(
                text = "Tap again on the selected star to clear it.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comment") },
                placeholder = { Text("Optional feedback about the ride") },
                minLines = 4
            )

            errorMessage?.let { message ->
                Surface(
                    color = ElectricGreen.copy(alpha = 0.10f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.20f))
                ) {
                    Text(
                        text = message,
                        color = ElectricGreen,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(text = "Skip")
                }

                Button(
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = ElectricGreen,
                        contentColor = WheelsSurface
                    )
                ) {
                    if (isSubmitting) {
                        Text(text = "Saving...")
                    } else {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(text = "Submit")
                    }
                }
            }
        }
    }
}
