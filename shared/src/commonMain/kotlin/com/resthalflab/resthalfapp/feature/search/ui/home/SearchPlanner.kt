package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.design.components.RhPickerField
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The reusable search planner card: destination, date range, guests + its sheets / range picker.
 * Guest and staff Home reuse this; only [searchButtonText] differs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPlanner(
    component: HomeComponent,
    searchButtonText: String,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    var showLocationSheet by remember { mutableStateOf(false) }
    var showOccupancySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    RhCard(modifier = modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
        FieldLabel("Location")
        RhPickerField(
            value = state.city,
            placeholder = "Where to stay?",
            onClick = {
                component.onLocationSheetOpened()
                showLocationSheet = true
            },
            leadingIcon = Icons.Outlined.LocationOn,
        )

        Spacer(Modifier.height(RhSpacing.md))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("Check-in")
                RhPickerField(
                    value = if (state.dateChosen) dayMonth(state.date) else "",
                    placeholder = "Add date",
                    onClick = { showDatePicker = true },
                    leadingIcon = Icons.Outlined.CalendarMonth,
                )
            }
            Spacer(Modifier.width(RhSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("Check-out")
                RhPickerField(
                    value = if (state.dateChosen) dayMonth(state.checkOut) else "",
                    placeholder = "Add date",
                    onClick = { showDatePicker = true },
                    leadingIcon = Icons.Outlined.CalendarMonth,
                )
            }
        }

        Spacer(Modifier.height(RhSpacing.md))
        FieldLabel("Guests")
        RhPickerField(
            value = if (state.guestsChosen) state.occupancy.summaryLabel() else "",
            placeholder = "1 room, 1 adult, 0 child",
            onClick = { showOccupancySheet = true },
            leadingIcon = Icons.Outlined.Group,
        )

        Spacer(Modifier.height(RhSpacing.xl))
        RhButton(
            text = searchButtonText,
            onClick = component::onSearchClicked,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showLocationSheet) {
        LocationSearchSheet(
            component = component,
            onDismiss = {
                showLocationSheet = false
                component.onLocationSheetDismissed()
            },
        )
    }

    if (showOccupancySheet) {
        OccupancySheet(
            occupancy = state.occupancy,
            onOccupancyChange = component::onOccupancyChanged,
            onDismiss = { showOccupancySheet = false },
        )
    }

    if (showDatePicker) {
        val todayUtcMillis = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            initialSelectedEndDateMillis = state.checkOut.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            // Block past dates — only today and future are bookable.
            selectableDates = remember(todayUtcMillis) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis >= todayUtcMillis
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null && end > start) {
                        component.onDateRangeSelected(start.toLocalDateUtc(), end.toLocalDateUtc())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DateRangePicker(state = rangeState, showModeToggle = false, modifier = Modifier.height(500.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = RhSpacing.xs, bottom = RhSpacing.xs),
    )
}

private fun dayMonth(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "${date.dayOfMonth} $month"
}

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
