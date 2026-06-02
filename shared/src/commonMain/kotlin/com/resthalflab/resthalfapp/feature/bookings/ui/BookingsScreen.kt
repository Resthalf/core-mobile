package com.resthalflab.resthalfapp.feature.bookings.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhOnWarningContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.design.RhSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhWarningContainer
import com.resthalflab.resthalfapp.core.design.components.RhScrollableTabs
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.onPrimaryContainer
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus

private val CancelledTagBg = Color(0xFFFFE0E0)
private val CancelledTagText = Color(0xFFB71C1C)

@Composable
fun BookingsScreen(component: BookingsComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val elevated by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BookingsHeader(
            selectedTabIndex = state.selectedTabIndex,
            elevated = elevated,
            onTabSelected = component::onTabSelected,
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                )
                state.error != null -> Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                )
                state.visibleBookings.isEmpty() -> Text(
                    text = "No bookings in this category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = RhSpacing.xl),
                ) {
                    items(state.visibleBookings, key = { it.id }) { booking ->
                        BookingRow(booking) { component.onBookingClicked(booking.id) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsHeader(
    selectedTabIndex: Int,
    elevated: Boolean,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (elevated) 4.dp else 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "My Bookings",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = RhSpacing.lg, top = RhSpacing.lg, bottom = RhSpacing.sm),
            )
            RhScrollableTabs(
                tabs = BookingTab.labels,
                selectedIndex = selectedTabIndex,
                onTabSelected = onTabSelected,
            )
            if (!elevated) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun BookingRow(booking: Booking, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail
        Surface(
            modifier = Modifier.size(width = 80.dp, height = 64.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            RhIllustrationPlaceholder(modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.width(RhSpacing.md))

        // Middle: hotel info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = booking.hotelName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = booking.city,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = booking.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = booking.stayWindow,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.xs))
            RhTag(
                text = booking.status.label,
                containerColor = booking.status.containerColor,
                contentColor = booking.status.contentColor,
            )
        }

        Spacer(Modifier.width(RhSpacing.sm))

        // Right: price + booking ID + chevron
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = formatMoney(booking.totalPrice, booking.currency),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Booking ID",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = booking.bookingCode,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp).size(20.dp),
        )
    }
}

// Helpers that keep the color logic out of the screen.
private val BookingStatus.label: String get() = when (this) {
    BookingStatus.Active -> "Active"
    BookingStatus.Completed -> "Completed"
    BookingStatus.Cancelled -> "Cancelled"
    BookingStatus.Overstayed -> "Overstayed"
}

private val BookingStatus.containerColor: Color get() = when (this) {
    BookingStatus.Active -> onPrimaryContainer
    BookingStatus.Completed -> RhSuccessContainer
    BookingStatus.Cancelled -> CancelledTagBg
    BookingStatus.Overstayed -> RhWarningContainer
}

private val BookingStatus.contentColor: Color get() = when (this) {
    BookingStatus.Active -> RhStarGold
    BookingStatus.Completed -> RhOnSuccessContainer
    BookingStatus.Cancelled -> CancelledTagText
    BookingStatus.Overstayed -> RhOnWarningContainer
}
