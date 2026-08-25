package com.resthalflab.resthalfapp.feature.bookings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhScrollableTabs
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus

private val PendingColor = Color(0xFF9A5B00)

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
                        contentPadding = PaddingValues(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                    ) {
                        if (state.visibleBookings.isEmpty()) {
                            item {
                                Text(
                                    text = "No trips here yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                )
                            }
                        } else {
                            items(state.visibleBookings, key = { it.id }) { booking ->
                                TripCard(booking) { component.onBookingClicked(booking.id) }
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
                text = "My Trips",
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
private fun TripCard(booking: Booking, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (booking.thumbnailUrl != null) {
                    RhRemoteImage(url = booking.thumbnailUrl, contentDescription = booking.hotelName, modifier = Modifier.fillMaxSize())
                } else {
                    RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
                }
                StatusBadge(
                    status = booking.status,
                    modifier = Modifier.align(Alignment.TopStart).padding(RhSpacing.md),
                )
            }

            Column(modifier = Modifier.padding(RhSpacing.lg)) {
                Text(
                    text = booking.hotelName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(RhSpacing.sm))
                InfoLine(Icons.Filled.CalendarMonth, booking.dateLabel)
                if (booking.city.isNotBlank()) {
                    Spacer(Modifier.height(RhSpacing.xs))
                    InfoLine(Icons.Outlined.LocationOn, booking.city)
                }

                if (booking.tags.isNotEmpty()) {
                    Spacer(Modifier.height(RhSpacing.md))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                    ) {
                        booking.tags.take(4).forEach { tag ->
                            RhTag(
                                text = tag,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(RhSpacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(RhSpacing.xs))
                        Text(
                            text = booking.guestsLabel.ifBlank { "1 guest" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    RhButton(
                        text = "View Itinerary",
                        onClick = {},
                        shape = RoundedCornerShape(percent = 80),
                        contentPadding = PaddingValues(horizontal = RhSpacing.sm, vertical = 0.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(RhSpacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val (label, accent) = when (status) {
        BookingStatus.Confirmed -> "BOOKED" to RhSuccess
        BookingStatus.Active -> "ACTIVE" to MaterialTheme.colorScheme.primary
        BookingStatus.Pending -> "PENDING" to PendingColor
        BookingStatus.Completed -> "COMPLETED" to RhSuccess
        BookingStatus.Cancelled -> "CANCELLED" to MaterialTheme.colorScheme.error
        BookingStatus.Overstayed -> "OVERSTAYED" to PendingColor
        BookingStatus.InternalError -> "FAILED" to MaterialTheme.colorScheme.error
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = RhSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(RhSpacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
        }
    }
}
