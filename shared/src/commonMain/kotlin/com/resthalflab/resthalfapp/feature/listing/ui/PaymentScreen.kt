package com.resthalflab.resthalfapp.feature.listing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhWebView
import com.resthalflab.resthalfapp.feature.listing.api.PaymentComponent

@Composable
fun PaymentScreen(component: PaymentComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = RhSpacing.sm, end = RhSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = component::onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Payment",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // The Midtrans Snap page lives here; replace SANDBOX_PAYMENT_URL with the real redirect URL.
        RhWebView(url = component.paymentUrl, modifier = Modifier.fillMaxWidth().weight(1f))

        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(RhSpacing.lg)) {
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(RhSpacing.sm))
            }
            Text(
                text = "Sandbox: tap to simulate a successful payment.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            RhButton(
                text = "Simulate Successful Payment",
                onClick = component::onSimulatePayment,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.internalError) {
        AlertDialog(
            onDismissRequest = component::onDismissInternalError,
            title = { Text("Internal Error") },
            text = { Text("There is error processing this booking, could be expired or have no slots") },
            confirmButton = {
                TextButton(onClick = component::onDismissInternalError) { Text("OK") }
            },
        )
    }
}
