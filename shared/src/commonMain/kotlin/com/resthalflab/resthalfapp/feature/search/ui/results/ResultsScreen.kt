package com.resthalflab.resthalfapp.feature.search.ui.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccessContainer
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRatingRow
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing

@Composable
fun ResultsScreen(component: ResultsComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val elevated by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ResultsHeader(
            destination = state.destination,
            dateLabel = state.dateLabel,
            stayWindow = state.stayWindow,
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
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = RhSpacing.xl),
                ) {
                    items(state.results, key = { it.id }) { listing ->
                        ListingRow(listing) { component.onListingClicked(listing.id) }
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
    destination: String,
    dateLabel: String,
    stayWindow: String,
    elevated: Boolean,
    onBack: () -> Unit,
    onFilter: () -> Unit,
) {
    // At rest: flat with a plain divider. While scrolling: drop shadow, no divider.
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (elevated) 4.dp else 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        text = destination.ifBlank { "All cities" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$dateLabel · $stayWindow",
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
                stayWindow = stayWindow,
                modifier = Modifier.padding(start = RhSpacing.lg, bottom = RhSpacing.md),
            )
            if (!elevated) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun NightStayChip(stayWindow: String, modifier: Modifier = Modifier) {
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
            Text(
                text = "Night Stay · $stayWindow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ListingRow(listing: Listing, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            HotelThumbnail()
            Spacer(Modifier.width(RhSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RhTag(
                        text = "Free cancellation",
                        containerColor = RhSuccessContainer,
                        contentColor = RhOnSuccessContainer,
                    )
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.height(RhSpacing.xs))
                Text(
                    text = listing.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = listing.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(RhSpacing.xs))
                RhRatingRow(rating = listing.rating, reviews = listing.reviewsCount)
            }
        }

        Spacer(Modifier.height(RhSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "Night Stay · 12AM–12PM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "1 room",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(listing.pricePerNight, listing.currency),
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
    }
}

@Composable
private fun HotelThumbnail() {
    Box(modifier = Modifier.size(96.dp)) {
        // Random-image placeholder; swap for RhRemoteImage(thumbnailUrl) when search returns photos.
        RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
