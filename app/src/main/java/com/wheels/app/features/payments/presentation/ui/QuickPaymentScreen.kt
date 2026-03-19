package com.wheels.app.features.payments.presentation.ui

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.payments.presentation.viewmodel.PaymentsViewModel
import java.text.NumberFormat
import java.util.Locale

private val SelectionBlue = Color(0xFF5B89C8)
private val SelectionBackground = Color(0xFFE8F0F9)

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val saved: Boolean,
    val details: String? = null
)

@Composable
fun QuickPaymentScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: PaymentsViewModel
) {
    var selectedMethod by remember { mutableStateOf("card") }

    val rideDetails = mapOf(
        "driver" to "Carlos Mendez",
        "from" to "Campus Uniandes",
        "to" to "Centro Comercial Andino",
        "date" to "Today, 14:30",
        "fare" to "3500"
    )

    val paymentMethods = listOf(
        PaymentMethod(
            id = "card",
            name = "Credit/Debit Card",
            icon = Icons.Outlined.CreditCard,
            saved = true,
            details = "•••• 4532"
        ),
        PaymentMethod(
            id = "wallet",
            name = "Digital Wallet",
            icon = Icons.Outlined.AccountBalanceWallet,
            saved = true,
            details = "Balance: $${formatCopAmount("15000")}"
        ),
        PaymentMethod(
            id = "qr",
            name = "QR Code Payment",
            icon = Icons.Outlined.QrCode2,
            saved = false,
            details = "Scan QR to pay"
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WheelsBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            item {
                HeaderSection(navController = navController)
            }

            item {
                AmountCard(
                    fare = rideDetails["fare"]!!,
                    modifier = Modifier.offset(y = (-20).dp)
                )
            }

            item {
                RideSummarySection(
                    rideDetails = rideDetails,
                    modifier = Modifier.offset(y = (-20).dp)
                )
            }

            item {
                PaymentMethodSection(
                    methods = paymentMethods,
                    selectedMethod = selectedMethod,
                    onMethodSelected = { selectedMethod = it },
                    modifier = Modifier.offset(y = (-20).dp)
                )
            }

            item {
                SecurityNoticeSection(modifier = Modifier.offset(y = (-20).dp))
            }
        }

        PaymentButton(
            fare = rideDetails["fare"]!!,
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
                .padding(horizontal = 20.dp, vertical = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { navController.popBackStack() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = WheelsSurface,
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.bodySmall,
                    color = WheelsSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        Column {
            Text(
                text = "Quick Payment",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = WheelsSurface
            )
            Text(
                text = "Fast and secure ride payment",
                style = MaterialTheme.typography.bodySmall,
                color = WheelsSurface.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
private fun AmountCard(fare: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WheelsSurface)
            .border(1.dp, Border, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Amount to pay",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${formatCopAmount(fare)}",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ElectricGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = ElectricGreen,
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Payment protected",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RideSummarySection(rideDetails: Map<String, String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Ride Summary",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = WheelsSurface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF5B89C8), PrimaryBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CM",
                            color = WheelsSurface,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = rideDetails["driver"]!!,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Driver",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RideDetailRow("From", rideDetails["from"]!!)
                    RideDetailRow("To", rideDetails["to"]!!)
                    RideDetailRow("Date & Time", rideDetails["date"]!!)
                }
            }
        }
    }
}

@Composable
private fun RideDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = PrimaryBlue
        )
    }
}

@Composable
private fun PaymentMethodSection(
    methods: List<PaymentMethod>,
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        methods.forEach { method ->
            PaymentMethodCard(
                method = method,
                isSelected = selectedMethod == method.id,
                onSelect = { onMethodSelected(method.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .border(
                width = 2.dp,
                color = if (isSelected) SelectionBlue else Border,
                shape = RoundedCornerShape(20.dp)
            ),
        color = if (isSelected) SelectionBackground else WheelsSurface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) SelectionBlue else WheelsBackground
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = null,
                    tint = if (isSelected) WheelsSurface else TextSecondary,
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                if (method.details != null) {
                    Text(
                        text = method.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (method.id == "wallet") ElectricGreen else TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) SelectionBlue else Border,
                        shape = CircleShape
                    )
                    .background(if (isSelected) SelectionBlue else WheelsSurface),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = WheelsSurface,
                        modifier = Modifier
                            .width(14.dp)
                            .height(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityNoticeSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SecondaryBlue.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = SecondaryBlue,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .padding(top = 2.dp)
            )
            Column {
                Text(
                    text = "Secure Payment",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Text(
                    text = "Your payment information is encrypted and secure. Funds are held until ride completion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PaymentButton(
    fare: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(WheelsSurface)
            .border(1.dp, Border)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "On-time bonus",
                    tint = ElectricGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "On-time payment bonus:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ElectricGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Earn +3 points",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { navController.popBackStack() }
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricGreen, Color(0xFF00c794))
                        )
                    ),
                color = ElectricGreen,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pay COP ${formatCopAmount(fare)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = WheelsSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = WheelsSurface,
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatCopAmount(rawAmount: String): String {
    val amount = rawAmount.toLongOrNull() ?: 0L
    return NumberFormat.getNumberInstance(Locale.US).format(amount)
}
