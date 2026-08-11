package com.resthalflab.resthalfapp.feature.staff.ui.checkins

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhScrollableTabs
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.ui.rememberRemainingLabel
import com.resthalflab.resthalfapp.feature.staff.domain.model.CheckIn
import com.resthalflab.resthalfapp.feature.staff.ui.ellipsize
import com.resthalflab.resthalfapp.feature.staff.ui.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInsScreen(component: CheckInsComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 0.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Check-ins",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(
                        start = RhSpacing.lg, top = RhSpacing.lg, bottom = RhSpacing.sm,
                    ),
                )
                RhScrollableTabs(
                    tabs = CheckInTab.labels,
                    selectedIndex = state.selectedTabIndex,
                    onTabSelected = component::onTabSelected,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = RhSpacing.xl),
                    ) {
                        if (state.visible.isEmpty()) {
                            item {
                                Text(
                                    text = "No check-ins in this category",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                )
                            }
                        } else {
                            items(state.visible, key = { it.id }) { checkIn ->
                                CheckInRow(checkIn)
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
private fun CheckInRow(checkIn: CheckIn) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
    ) {
        Text(
            text = "Booking ${checkIn.bookingId.ellipsize(15)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = BookingTime.formatWindow(checkIn.startTime, checkIn.endTime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(RhSpacing.xs))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs),
        ) {
            RhTag(
                text = checkIn.slotType.label,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            RhTag(text = if (checkIn.active) "Active" else "Completed")
            if (checkIn.active) {
                Text(
                    text = rememberRemainingLabel(checkIn.endTime),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
