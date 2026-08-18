package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhStepper
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy

/** Rooms / adults / children steppers. Updates are applied live via [onOccupancyChange]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OccupancySheet(
    occupancy: Occupancy,
    onOccupancyChange: (Occupancy) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg)) {
            Text(
                text = "Guests",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(RhSpacing.lg))
            RhStepper(
                label = "Rooms",
                value = occupancy.rooms,
                onValueChange = { onOccupancyChange(occupancy.copy(rooms = it)) },
                min = Occupancy.MIN_ROOMS,
                max = Occupancy.MAX_ROOMS,
            )
            Spacer(Modifier.height(RhSpacing.md))
            RhStepper(
                label = "Adults",
                subtitle = "Ages 18+",
                value = occupancy.adults,
                onValueChange = { onOccupancyChange(occupancy.copy(adults = it)) },
                min = Occupancy.MIN_ADULTS,
                max = Occupancy.MAX_ADULTS,
            )
            Spacer(Modifier.height(RhSpacing.md))
            RhStepper(
                label = "Children",
                subtitle = "Ages 0–17",
                value = occupancy.children,
                onValueChange = { onOccupancyChange(occupancy.copy(children = it)) },
                min = Occupancy.MIN_CHILDREN,
                max = Occupancy.MAX_CHILDREN,
            )
            Spacer(Modifier.height(RhSpacing.xl))
            RhButton(text = "Done", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }
}

/** "1 Room, 2 Adults, 0 Children" — the summary shown on the planner picker row. */
internal fun Occupancy.summaryLabel(): String {
    fun plural(count: Int, singular: String, plural: String) = "$count ${if (count == 1) singular else plural}"
    return listOf(
        plural(rooms, "Room", "Rooms"),
        plural(adults, "Adult", "Adults"),
        plural(children, "Child", "Children"),
    ).joinToString(", ")
}
