package com.resthalflab.resthalfapp.feature.listing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnWarningContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.RhWarningContainer
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhInfoBanner
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedCard
import com.resthalflab.resthalfapp.core.design.components.RhTextButton
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmation
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent.State

@Composable
fun BookingConfirmationScreen(component: BookingConfirmationComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        IconButton(onClick = component::onBackClicked, modifier = Modifier.padding(RhSpacing.sm)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                State.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is State.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    RhButton(text = "Retry", onClick = component::onRetry)
                }
                is State.Content -> Content(s.confirmation, component)
            }
        }
    }
}

@Composable
private fun Content(confirmation: BookingConfirmation, component: BookingConfirmationComponent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = RhSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val paid = confirmation.paid

        Spacer(Modifier.height(RhSpacing.sm))
        Box(
            modifier = Modifier.size(64.dp).background(
                color = if (paid) RhSuccess else RhWarningContainer,
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (paid) Icons.Filled.Check else Icons.Outlined.Schedule,
                contentDescription = null,
                tint = if (paid) Color.White else RhOnWarningContainer,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(RhSpacing.lg))
        Text(
            text = if (paid) "Booking Success" else "Booking Created",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = if (paid) {
                "Your room at ${confirmation.hotelName} is booked."
            } else {
                "Your stay is ready, please complete the payment."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(RhSpacing.lg))
        DetailsCard(confirmation)

        if (!paid) {
            Spacer(Modifier.height(RhSpacing.lg))
            RhInfoBanner(
                subtitle = "Your room is held briefly. Complete payment to confirm your booking.",
                leadingIcon = Icons.Outlined.Schedule,
                containerColor = RhWarningContainer,
                contentColor = RhOnWarningContainer,
                iconTint = RhOnWarningContainer,
            )
        }

        Spacer(Modifier.height(RhSpacing.xl))
        if (paid) {
            RhButton(
                text = "View Details",
                onClick = component::onViewDetails,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RhSpacing.md),
            ) {
                RhOutlinedButton(
                    text = "View Details",
                    onClick = component::onViewDetails,
                    modifier = Modifier.weight(1f),
                )
                RhButton(
                    text = "Proceed to Payment",
                    onClick = component::onProceedToPayment,
                    modifier = Modifier.weight(1.5f),
                )
            }
        }
        Spacer(Modifier.height(RhSpacing.lg))
    }
}

@Composable
private fun DetailsCard(confirmation: BookingConfirmation) {
    val clipboard = LocalClipboardManager.current
    RhOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Booking ID",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = confirmation.bookingId,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            RhTextButton(
                text = "Copy",
                onClick = { clipboard.setText(AnnotatedString(confirmation.bookingId)) },
            )
        }

        Spacer(Modifier.height(RhSpacing.md))
        DetailRow("Hotel", confirmation.hotelName)
        DetailRow("Room Type", confirmation.roomType)
        DetailRow("Date", confirmation.dateLabel)
        DetailRow("Stay Window", confirmation.stayWindow)
        DetailRow("Guests", confirmation.guestsLabel)
        DetailRow(
            label = if (confirmation.paid) "Total Paid" else "Total",
            value = confirmation.totalPaid,
            valueColor = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}
