package com.resthalflab.resthalfapp.feature.search.ui.results

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel

@Composable
fun ResultsScreen(component: ResultsComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val favoriteIds by component.favoriteHotelIds.collectAsStateWithLifecycle()
    var showFilter by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Applying a new filter/sort changes the whole list — snap back to the top.
    LaunchedEffect(state.filters, state.sort) {
        if (state.visibleResults.isNotEmpty()) listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ResultsHeader(
            state = state,
            onBack = component::onBackClicked,
            onFilterClick = { showFilter = true },
            onSortClick = { showSort = true },
            onQuickFilter = component::onApplyFilters,
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.loading -> Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    CircularProgressIndicator()
                    Text("Searching hotels…", style = MaterialTheme.typography.bodyMedium)
                }

                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp, start = RhSpacing.xl, end = RhSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = component::onRetry) { Text("Retry") }
                }

                state.results.isEmpty() -> Text(
                    text = "No hotels found for these dates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                )

                state.visibleResults.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp, start = RhSpacing.xl, end = RhSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    Text(
                        text = "No hotels match your filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = { component.onApplyFilters(HotelFilters()) }) { Text("Clear filters") }
                }

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(RhSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
                ) {
                    items(state.visibleResults, key = { it.id }) { hotel ->
                        HotelCard(
                            hotel = hotel,
                            isFavorite = hotel.id in favoriteIds,
                            onToggleFavorite = { component.onToggleFavorite(hotel) },
                            onView = { component.onViewHotel(hotel) },
                        )
                    }
                }
            }
        }
    }

    if (showFilter) {
        state.facets?.let { facets ->
            FilterSheet(
                facets = facets,
                current = state.filters,
                locationLabel = state.locationLabel,
                onApply = {
                    component.onApplyFilters(it)
                    showFilter = false
                },
                onDismiss = { showFilter = false },
            )
        }
    }
    if (showSort) {
        SortSheet(
            current = state.sort,
            onSelect = {
                component.onSortSelected(it)
                showSort = false
            },
            onDismiss = { showSort = false },
        )
    }
}

@Composable
private fun ResultsHeader(
    state: ResultsComponent.UiState,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onQuickFilter: (HotelFilters) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = RhSpacing.sm, end = RhSpacing.lg, top = RhSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Showing results for",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.locationLabel,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(RhSpacing.xs))
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = "${state.dateLabel} · ${state.guestsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Filter / Sort actions + popular quick toggles. Hidden until results arrive.
            if (state.results.isNotEmpty()) {
                val filters = state.filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                ) {
                    AssistChip(
                        onClick = onFilterClick,
                        shape = RhRadius.button,
                        leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = {
                            Text(if (state.activeFilterCount > 0) "Filter · ${state.activeFilterCount}" else "Filter")
                        },
                    )
                    AssistChip(
                        onClick = onSortClick,
                        shape = RhRadius.button,
                        leadingIcon = { Icon(Icons.Outlined.SwapVert, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = {
                            Text(if (state.sort == SortOption.Recommended) "Sort" else state.sort.label)
                        },
                    )
                    FilterChip(
                        selected = filters.freeCancellation,
                        onClick = { onQuickFilter(filters.copy(freeCancellation = !filters.freeCancellation)) },
                        label = { Text("Free cancellation") },
                        shape = RhRadius.button,
                    )
                    FilterChip(
                        selected = filters.freeBreakfast,
                        onClick = { onQuickFilter(filters.copy(freeBreakfast = !filters.freeBreakfast)) },
                        label = { Text("Free breakfast") },
                        shape = RhRadius.button,
                    )
                    FilterChip(
                        selected = filters.deals,
                        onClick = { onQuickFilter(filters.copy(deals = !filters.deals)) },
                        label = { Text("Deals") },
                        shape = RhRadius.button,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HotelCard(
    hotel: WholesaleHotel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onView: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        HotelCardBody(
            imageWidth = 112.dp,
            modifier = Modifier.padding(RhSpacing.sm),
            image = {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))) {
                if (hotel.imageUrl != null) {
                    RhRemoteImage(url = hotel.imageUrl, contentDescription = hotel.name, modifier = Modifier.fillMaxSize())
                } else {
                    RhIllustrationPlaceholder(modifier = Modifier.fillMaxSize())
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).padding(RhSpacing.xs).size(28.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (hotel.rating != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(RhSpacing.xs),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = RhStarGold, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = formatRating(hotel.rating),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
                }
            },
            content = {
                Column(modifier = Modifier.padding(start = RhSpacing.md)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = (hotel.category ?: "Hotel").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    hotel.offerText?.let {
                        RhTag(
                            text = it,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                Spacer(Modifier.height(RhSpacing.xs))
                Text(
                    text = hotel.name ?: "Hotel ${hotel.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hotel.facilities.isNotEmpty()) {
                    Spacer(Modifier.height(RhSpacing.xs))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                    ) {
                        hotel.facilities.take(3).forEach { facility ->
                            RhTag(
                                text = facility,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    (hotel.boardBasis ?: hotel.address)?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (hotel.freeCancellation) {
                    Text(
                        text = "Free cancellation",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = RhSuccess,
                    )
                }

                Spacer(Modifier.height(RhSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = "${formatMoney(hotel.perNightRate, hotel.currency)} / night",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${formatMoney(hotel.totalRate, hotel.currency)} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = onView,
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(horizontal = RhSpacing.md, vertical = 0.dp),
                        modifier = Modifier.heightIn(min = 34.dp),
                    ) {
                        Text("View", style = MaterialTheme.typography.labelMedium)
                    }
                }
                }
            },
        )
    }
}

/**
 * Card row that draws [image] beside [content] and sizes the image to the content's measured height,
 * so the image always fills the card — no bottom gap, no clipping — no matter how the text/chips wrap.
 */
@Composable
private fun HotelCardBody(
    imageWidth: Dp,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Layout(contents = listOf(image, content), modifier = modifier) { (imageMeasurables, contentMeasurables), constraints ->
        val imageWidthPx = imageWidth.roundToPx()
        val contentWidth = (constraints.maxWidth - imageWidthPx).coerceAtLeast(0)
        val contentPlaceable = contentMeasurables.first().measure(
            constraints.copy(minWidth = contentWidth, maxWidth = contentWidth),
        )
        val height = contentPlaceable.height
        val imagePlaceable = imageMeasurables.first().measure(Constraints.fixed(imageWidthPx, height))
        layout(constraints.maxWidth, height) {
            imagePlaceable.place(0, 0)
            contentPlaceable.place(imageWidthPx, 0)
        }
    }
}

private fun formatRating(rating: Double): String =
    if (rating % 1.0 == 0.0) rating.toInt().toString() else rating.toString()
