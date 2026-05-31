package com.resthalflab.resthalfapp.feature.bookings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhScaffold

@Composable
fun BookingsScreen(@Suppress("UNUSED_PARAMETER") component: BookingsComponent) {
    RhScaffold(title = "Bookings") { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RhSpacing.sm),
            ) {
                Text("No bookings yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Your upcoming and past day-room bookings will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
