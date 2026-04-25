package com.wheels.app.features.reviews.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerViewModel

@Composable
fun ReviewComposerRoute(
    modifier: Modifier = Modifier
) {
    val viewModel = remember { ReviewComposerViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    ReviewComposerScreen(
        modifier = modifier,
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

