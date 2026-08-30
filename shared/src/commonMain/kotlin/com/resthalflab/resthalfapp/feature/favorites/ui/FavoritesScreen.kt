package com.resthalflab.resthalfapp.feature.favorites.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun FavoritesScreen(component: FavoritesComponent) {
    val favorites by component.favorites.collectAsStateWithLifecycle()
    val planning by component.planning.collectAsStateWithLifecycle()
    // The favorite the user is currently choosing dates for (drives the range-picker dialog).
    var datePickerFor by remember { mutableStateOf<FavoriteHotel?>(null) }
    val planningInProgress = planning.loadingHotelId != null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FavoritesHeader()

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (favorites.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = RhSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                    ) {
                        Text("No saved destinations yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap the heart on a stay to save it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(RhSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.lg),
                    ) {
                        items(favorites, key = { it.hotelId }) { favorite ->
                            FavoriteCard(
                                favorite = favorite,
                                canPlanTrip = component.canPlanTrip(favorite) && !planningInProgress,
                                onRemove = { component.onRemove(favorite.hotelId) },
                                onPlanTrip = { datePickerFor = favorite },
                            )
                        }
                    }
                }
            }
        }

        if (planningInProgress) {
            PlanningOverlay()
        }
    }

    datePickerFor?.let { favorite ->
        PlanTripDatePicker(
            onConfirm = { checkIn, checkOut ->
                datePickerFor = null
                component.onPlanTrip(favorite, checkIn, checkOut)
            },
            onDismiss = { datePickerFor = null },
        )
    }

    planning.error?.let { message ->
        AlertDialog(
            onDismissRequest = component::onDismissPlanningError,
            confirmButton = { TextButton(onClick = component::onDismissPlanningError) { Text("OK") } },
            title = { Text("Couldn't load rooms") },
            text = { Text(message) },
        )
    }
}

/** Full-screen scrim shown while "Plan Trip" runs the search that unlocks the hotel's rooms. */
@Composable
private fun PlanningOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            // Swallow taps so nothing behind the scrim is interactive while we load.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Column(
                modifier = Modifier.padding(RhSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
            ) {
                CircularProgressIndicator()
                Text("Finding available rooms…", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FavoritesHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(start = RhSpacing.lg, end = RhSpacing.lg, top = RhSpacing.lg, bottom = RhSpacing.sm)) {
        Text(
            text = "Saved Destinations",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Your curated list of upcoming adventures.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FavoriteCard(
    favorite: FavoriteHotel,
    canPlanTrip: Boolean,
    onRemove: () -> Unit,
    onPlanTrip: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (favorite.imageUrl != null) {
                    RhRemoteImage(url = favorite.imageUrl, contentDescription = favorite.hotelName, modifier = Modifier.fillMaxSize())
                } else {
                    RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
                }

                // Saved heart — tapping removes it from favorites.
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(RhSpacing.md).size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                ) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Remove from saved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (favorite.rating != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(RhSpacing.md),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = RhSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = RhStarGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = formatRating(favorite.rating),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(RhSpacing.lg)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = favorite.hotelName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    val tag = favorite.location?.country?.takeIf { it.isNotBlank() } ?: favorite.city
                    if (tag.isNotBlank()) {
                        Spacer(Modifier.width(RhSpacing.sm))
                        RhTag(text = tag)
                    }
                }

                val subtitle = favorite.location?.fullName?.takeIf { it.isNotBlank() } ?: favorite.city
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(RhSpacing.xs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(RhSpacing.xs))
                Text(
                    text = "from ${formatMoney(favorite.fromPrice, favorite.currency)} / night",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(RhSpacing.md))
                FilledTonalButton(
                    onClick = onPlanTrip,
                    enabled = canPlanTrip,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text("Plan Trip", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/** Date-range picker for "Plan Trip": pick check-in → check-out, then fire the search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanTripDatePicker(
    onConfirm: (checkIn: LocalDate, checkOut: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val todayUtcMillis = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    val oneDayMillis = 24L * 60 * 60 * 1000
    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = todayUtcMillis,
        initialSelectedEndDateMillis = todayUtcMillis + oneDayMillis,
        // Only today and future are bookable.
        selectableDates = remember(todayUtcMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val start = rangeState.selectedStartDateMillis
                val end = rangeState.selectedEndDateMillis
                if (start != null && end != null && end > start) {
                    onConfirm(start.toLocalDateUtc(), end.toLocalDateUtc())
                } else {
                    onDismiss()
                }
            }) { Text("Search") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DateRangePicker(state = rangeState, showModeToggle = false, modifier = Modifier.height(500.dp))
    }
}

private fun formatRating(rating: Double): String =
    if (rating % 1.0 == 0.0) rating.toInt().toString() else rating.toString()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
