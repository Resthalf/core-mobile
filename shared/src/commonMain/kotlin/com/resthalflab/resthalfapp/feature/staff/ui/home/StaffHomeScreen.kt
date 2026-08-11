package com.resthalflab.resthalfapp.feature.staff.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhBrandTopBar
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedCard
import com.resthalflab.resthalfapp.core.design.components.RhSectionHeader
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.feature.bookings.ui.rememberRemainingLabel
import com.resthalflab.resthalfapp.feature.search.ui.home.SearchPlanner
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus

@Composable
fun StaffHomeScreen(component: StaffHomeComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    var vacateTarget by remember { mutableStateOf<RoomStatus?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        RhBrandTopBar(onActionClick = component::onRefresh)

        StaffHero()

        SearchPlanner(
            component = component.home,
            searchButtonText = "Search Manual Book Rooms",
        )

        NextVacatedSection(
            state = state,
            onViewDetails = component::onViewDetails,
            onConfirmVacate = { vacateTarget = it },
        )
    }

    vacateTarget?.let { room ->
        VacateDialog(
            room = room,
            onDismiss = { vacateTarget = null },
            onConfirm = { notes ->
                room.delegationId?.let { component.onConfirmVacate(room.roomId, it, notes) }
                vacateTarget = null
            },
        )
    }
}

@Composable
private fun StaffHero() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
        Text(
            text = "Front desk",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(RhSpacing.xs))
        Text(
            text = "Book rooms manually and turn over today's stays.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextVacatedSection(
    state: StaffHomeComponent.UiState,
    onViewDetails: (String) -> Unit,
    onConfirmVacate: (RoomStatus) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
        RhSectionHeader("Next vacated rooms")
        Spacer(Modifier.height(RhSpacing.lg))

        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text(
                text = state.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            state.rooms.isEmpty() -> Text(
                text = "No rooms are due to be vacated soon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> state.rooms.forEach { room ->
                VacatedRoomCard(
                    room = room,
                    vacating = state.vacatingRoomId == room.roomId,
                    onViewDetails = { onViewDetails(room.roomId) },
                    onConfirmVacate = { onConfirmVacate(room) },
                )
                Spacer(Modifier.height(RhSpacing.md))
            }
        }
        Spacer(Modifier.height(RhSpacing.lg))
    }
}

@Composable
private fun VacatedRoomCard(
    room: RoomStatus,
    vacating: Boolean,
    onViewDetails: () -> Unit,
    onConfirmVacate: () -> Unit,
) {
    RhOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Room ${room.roomNumber} · ${room.roomType}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = room.hotelName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RhTag(text = room.currentStatus)
        }

        room.endTime?.let { end ->
            Spacer(Modifier.height(RhSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(18.dp),
                )
                Spacer(Modifier.width(RhSpacing.xs))
                Text(
                    text = rememberRemainingLabel(end),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(RhSpacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm)) {
            RhOutlinedButton(
                text = "View Details",
                onClick = onViewDetails,
                modifier = Modifier.weight(1f),
            )
            RhButton(
                text = "Confirm Vacate",
                onClick = onConfirmVacate,
                loading = vacating,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VacateDialog(
    room: RoomStatus,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm vacate") },
        text = {
            Column {
                Text(
                    text = "Mark room ${room.roomNumber} as vacated?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(RhSpacing.md))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Guest left at 14:05") },
                    keyboardOptions = KeyboardOptions.Default,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(notes) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
