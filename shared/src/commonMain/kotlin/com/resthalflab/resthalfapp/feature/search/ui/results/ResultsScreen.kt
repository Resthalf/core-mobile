package com.resthalflab.resthalfapp.feature.search.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRatingRow
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult
import com.resthalflab.resthalfapp.feature.search.domain.model.RoomOption
import com.resthalflab.resthalfapp.feature.search.domain.stayTitle
import com.resthalflab.resthalfapp.feature.search.domain.windowShort

@Composable
fun ResultsScreen(component: ResultsComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val expandedIds = remember { mutableStateMapOf<String, Boolean>() }
    val elevated by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ResultsHeader(
            city = state.city,
            dateLabel = state.dateLabel,
            stayTitle = state.stayTitle,
            windowShort = state.windowShort,
            elevated = elevated,
            onBack = component::onBackClicked,
            onFilter = { /* TODO Phase 3: filters */ },
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                )
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    RhButton(text = "Retry", onClick = component::onRetry)
                }
                state.results.isEmpty() -> Text(
                    text = "No rooms available for this date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(bottom = RhSpacing.xl),
                ) {
                    items(state.results, key = { it.hotelId }) { hotel ->
                        HotelRow(
                            hotel = hotel,
                            expanded = expandedIds[hotel.hotelId] == true,
                            onToggleExpand = {
                                expandedIds[hotel.hotelId] = expandedIds[hotel.hotelId] != true
                            },
                            onRoomClick = { component.onHotelClicked(hotel.hotelId) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    item {
                        Text(
                            text = "${state.results.size} hotels found",
                            modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.lg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    city: String,
    dateLabel: String,
    stayTitle: String,
    windowShort: String,
    elevated: Boolean,
    onBack: () -> Unit,
    onFilter: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (elevated) 4.dp else 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = RhSpacing.sm, end = RhSpacing.lg, top = RhSpacing.sm, bottom = RhSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = city.ifBlank { "All cities" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$dateLabel · $windowShort",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = onFilter,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = RhSpacing.md, vertical = RhSpacing.sm),
                ) {
                    Text("Filter", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(RhSpacing.xs))
                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            NightStayChip(
                label = "$stayTitle · $windowShort",
                modifier = Modifier.padding(start = RhSpacing.lg, bottom = RhSpacing.md),
            )
            if (!elevated) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun NightStayChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = RhSpacing.md, vertical = RhSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Bedtime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(RhSpacing.xs))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun HotelRow(
    hotel: HotelSearchResult,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRoomClick: (RoomOption) -> Unit,
) {
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
    ) {
        // Top: image · (badge, name, city, rating) · favourite
        Row(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(width = 96.dp, height = 96.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                RhIllustrationPlaceholder(modifier = Modifier.fillMaxSize())
            }

            Spacer(Modifier.width(RhSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                hotel.badge?.let {
                    RhTag(text = it)
                    Spacer(Modifier.height(RhSpacing.xs))
                }
                Text(
                    text = hotel.hotelName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = hotel.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Rating row appears once /search returns rating/reviews.
//                if (hotel.rating != null) {
                    Spacer(Modifier.height(RhSpacing.xs))
                    RhRatingRow(rating = hotel.rating ?: 5.0, reviews = hotel.reviewsCount ?: 0)
//                }
            }

            FavouriteButton()
        }

        Spacer(Modifier.height(RhSpacing.md))

        // Bottom: slot · window / rooms  —  price / room
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "${hotel.slotType.stayTitle} · ${hotel.slotType.windowShort}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (hotel.roomCount == 1) "1 room available" else "${hotel.roomCount} rooms available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(hotel.fromPrice, hotel.currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "/room",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Expandable room sub-list
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = RhSpacing.md),
                verticalArrangement = Arrangement.spacedBy(RhSpacing.sm),
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(RhSpacing.xs))
                hotel.rooms.forEach { room ->
                    RoomItem(room = room, onClick = { onRoomClick(room) })
                }
            }
        }

        // Bottom-centered chevron toggles the room list.
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Hide rooms" else "Show rooms",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
        }
    }
}

@Composable
private fun RoomItem(room: RoomOption, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.md, vertical = RhSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Bed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(RhSpacing.sm))
                Column {
                    Text(
                        text = "Room ${room.roomNumber}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = room.slotType.stayTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatMoney(room.price, room.currency),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Local-only toggle for now; wire to the favourites feature when it lands.
@Composable
private fun FavouriteButton() {
    var favourite by remember { mutableStateOf(false) }
    IconButton(onClick = { favourite = !favourite }) {
        Icon(
            imageVector = if (favourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Save",
            tint = if (favourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
