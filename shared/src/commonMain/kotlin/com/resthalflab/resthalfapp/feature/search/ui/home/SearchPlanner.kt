package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.design.components.RhInfoBanner
import com.resthalflab.resthalfapp.core.design.components.RhPickerField
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.search.domain.formatLongDate
import com.resthalflab.resthalfapp.feature.search.domain.stayTitle
import com.resthalflab.resthalfapp.feature.search.domain.windowLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The reusable search planner card (city, date, guests, stay window) + its bottom sheets / date
 * picker. Guest and staff Home reuse this; only [searchButtonText] differs.
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
    var showSlotSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    RhCard(modifier = modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
        RhPickerField(
            value = state.city,
            placeholder = "Where to stay?",
            onClick = {
                component.onLocationSheetOpened()
                showLocationSheet = true
            },
            leadingIcon = Icons.Outlined.LocationOn,
            trailingIcon = Icons.Outlined.MyLocation,
        )

        Spacer(Modifier.height(RhSpacing.md))
        RhPickerField(
            value = if (state.dateChosen) formatLongDate(state.date) else "",
            placeholder = "1 night, 2 night, or else?",
            onClick = { showDatePicker = true },
            leadingIcon = Icons.Outlined.CalendarMonth,
            trailingIcon = Icons.Outlined.CalendarMonth,
        )

        Spacer(Modifier.height(RhSpacing.md))
        RhPickerField(
            value = if (state.guestsChosen) state.occupancy.summaryLabel() else "",
            placeholder = "1 room, 1 adult, 0 child",
            onClick = { showOccupancySheet = true },
            leadingIcon = Icons.Outlined.Group,
        )

        Spacer(Modifier.height(RhSpacing.md))
        RhInfoBanner(
            title = state.slotType.stayTitle,
            subtitle = state.slotType.windowLong,
            leadingIcon = state.slotType.bannerIcon(),
            modifier = Modifier.clickable { showSlotSheet = true },
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

    if (showSlotSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSlotSheet = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            Text(
                text = "Stay window",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = RhSpacing.lg, vertical = RhSpacing.sm),
            )
            SlotType.entries.forEach { slot ->
                SlotOption(
                    slot = slot,
                    selected = state.slotType == slot,
                    onClick = {
                        component.onSlotTypeSelected(slot)
                        showSlotSheet = false
                    },
                )
            }
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }

    if (showDatePicker) {
        val todayUtcMillis = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
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
                    datePickerState.selectedDateMillis?.let { component.onDateSelected(it.toLocalDateUtc()) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SlotOption(slot: SlotType, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(slot.bannerIcon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f).padding(start = RhSpacing.md)) {
            Text(slot.stayTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = slot.windowLong,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

internal fun SlotType.bannerIcon() = when (this) {
    SlotType.HALF_DAY -> Icons.Outlined.Bedtime
    SlotType.FULL_DAY -> Icons.Outlined.LightMode
}

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
