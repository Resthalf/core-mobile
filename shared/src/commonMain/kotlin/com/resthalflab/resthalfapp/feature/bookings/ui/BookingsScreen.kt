package com.resthalflab.resthalfapp.feature.bookings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val PendingTagBg = Color(0xFFFFE8CC)
private val PendingTagText = Color(0xFF9A5B00)
private val ConfirmedTagBg = Color(0xFFE3F0FF)
private val ConfirmedTagText = Color(0xFF1A4E8A)

@OptIn(ExperimentalMaterial3Api::class)
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
                else -> PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = component::onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = RhSpacing.xl),
                    ) {
                        if (state.visibleBookings.isEmpty()) {
                            item {
                                Text(
                                    text = "No bookings in this category",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                )
                            }
                        } else {
                            items(state.visibleBookings, key = { it.id }) { booking ->
                                BookingRow(booking) { component.onBookingClicked(booking.id) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookingRow(booking: Booking, onClick: () -> Unit) {
    // A failed or cancelled booking can't be opened or acted on — mute it and swallow taps.
    val disabled = booking.status == BookingStatus.InternalError ||
        booking.status == BookingStatus.Cancelled
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !disabled, onClick = onClick)
            .alpha(if (disabled) 0.5f else 1f)
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        // Thumbnail spans the full row height.
        Surface(
            modifier = Modifier.size(width = 88.dp, height = 100.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            RhIllustrationPlaceholder(modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.width(RhSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            // Title block: name (up to 2 lines) + city, spanning the full content width.
            Text(
                text = booking.hotelName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = booking.city,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(RhSpacing.sm))

            // Detail block: stay info (left) and price/booking-id (right).
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = booking.stayWindow,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(RhSpacing.lg))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        RhTag(
                            text = booking.slotTypeLabel,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        RhTag(
                            text = booking.status.label,
                            containerColor = booking.status.containerColor,
                            contentColor = booking.status.contentColor,
                        )
                    }
                }

                Spacer(Modifier.width(RhSpacing.sm))

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                        text = booking.bookingCode.ellipsize(10),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (booking.status == BookingStatus.Active) {
                        Text(
                            text = rememberRemainingLabel(booking.endTime),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = RhSpacing.xs, top = RhSpacing.xl).size(20.dp),
        )
    }
}

// Cap a string at [max] characters, appending an ellipsis when truncated.
private fun String.ellipsize(max: Int): String = if (length > max) take(max) + "…" else this

// Localized slot label, e.g. "HALF_DAY" -> "Half Day".
private val Booking.slotTypeLabel: String get() = when (slotType.uppercase()) {
    "HALF_DAY" -> "Half Day"
    "FULL_DAY" -> "Full Day"
    else -> slotType
}

// Helpers that keep the color logic out of the screen.
private val BookingStatus.label: String get() = when (this) {
    BookingStatus.Pending -> "Pending payment"
    BookingStatus.Confirmed -> "Confirmed"
    BookingStatus.Active -> "Active"
    BookingStatus.Completed -> "Completed"
    BookingStatus.Cancelled -> "Cancelled"
    BookingStatus.Overstayed -> "Overstayed"
    BookingStatus.InternalError -> "Internal Error"
}

private val BookingStatus.containerColor: Color get() = when (this) {
    BookingStatus.Pending -> PendingTagBg
    BookingStatus.Confirmed -> ConfirmedTagBg
    BookingStatus.Active -> onPrimaryContainer
    BookingStatus.Completed -> RhSuccessContainer
    BookingStatus.Cancelled -> CancelledTagBg
    BookingStatus.Overstayed -> RhWarningContainer
    BookingStatus.InternalError -> CancelledTagBg
}

private val BookingStatus.contentColor: Color get() = when (this) {
    BookingStatus.Pending -> PendingTagText
    BookingStatus.Confirmed -> ConfirmedTagText
    BookingStatus.Active -> RhStarGold
    BookingStatus.Completed -> RhOnSuccessContainer
    BookingStatus.Cancelled -> CancelledTagText
    BookingStatus.Overstayed -> RhOnWarningContainer
    BookingStatus.InternalError -> CancelledTagText
}
