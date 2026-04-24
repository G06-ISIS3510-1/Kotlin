package com.wheels.app.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.auth.presentation.viewmodel.CreateAccountEvent
import com.wheels.app.features.auth.presentation.viewmodel.CreateAccountViewModel

@Composable
fun CreateAccountScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: CreateAccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage

    LaunchedEffect(state.accountCreated) {
        if (state.accountCreated) {
            navController.navigate(Destinations.Home.route) {
                popUpTo(Destinations.CreateAccount.route) { inclusive = true }
            }
            viewModel.onEvent(CreateAccountEvent.ConsumeNavigation)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F0F9), WheelsSurface)))
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        IconButton(
            onClick = {
                if (!navController.popBackStack()) {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.CreateAccount.route) { inclusive = true }
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryBlue, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                AuthBrandLogo()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your Wheels profile and start moving around campus.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = WheelsSurface,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WheelsInputField(
                    value = state.fullName,
                    onValueChange = { viewModel.onEvent(CreateAccountEvent.FullNameChanged(it)) },
                    label = "Full Name",
                    placeholder = "Your full name",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )

                WheelsInputField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(CreateAccountEvent.EmailChanged(it)) },
                    label = "University Email",
                    placeholder = "student@university.edu",
                    keyboardType = KeyboardType.Email,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )

                WheelsInputField(
                    value = state.phone,
                    onValueChange = { viewModel.onEvent(CreateAccountEvent.PhoneChanged(it)) },
                    label = "Phone Number",
                    placeholder = "Your phone number",
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )

                WheelsInputField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(CreateAccountEvent.PasswordChanged(it)) },
                    label = "Password",
                    placeholder = "At least 8 characters",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )

                WheelsInputField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onEvent(CreateAccountEvent.ConfirmPasswordChanged(it)) },
                    label = "Confirm Password",
                    placeholder = "Repeat your password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { viewModel.onEvent(CreateAccountEvent.Submit) },
                    enabled = state.isFormFilled && !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WheelsSurface,
                        disabledContainerColor = Border,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = WheelsSurface
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                HorizontalDivider(color = Border)

                Text(
                    text = "By continuing, you accept the campus mobility rules and the Wheels community guidelines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Already have an account? Sign In",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.CreateAccount.route) { inclusive = true }
                    }
                }
                .padding(vertical = 8.dp)
        )
    }
}


