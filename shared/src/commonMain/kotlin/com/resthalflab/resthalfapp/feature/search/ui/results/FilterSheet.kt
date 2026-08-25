package com.resthalflab.resthalfapp.feature.search.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.domain.formatMoney

/**
 * Filter bottom sheet. Edits a local [HotelFilters] draft seeded from [current]; nothing is applied
 * until "Show result" is tapped. "Reset" clears the draft back to no filters. Sections that have no
 * choices in the current result set are hidden.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    facets: FilterFacets,
    current: HotelFilters,
    onApply: (HotelFilters) -> Unit,
    onDismiss: () -> Unit,
    locationLabel: String,
) {
    var draft by remember(current) { mutableStateOf(current) }
    var facilitiesExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.94f)) {
            // ── Header ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = RhSpacing.sm, end = RhSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { draft = HotelFilters() }) { Text("Reset") }
            }

            // ── Scrollable sections ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RhSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
            ) {
                Spacer(Modifier.height(RhSpacing.xs))

                // Popular — quick boolean toggles.
                FilterSection(title = "Popular filters in $locationLabel") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                    ) {
                        ToggleChip("Free cancellation", draft.freeCancellation) {
                            draft = draft.copy(freeCancellation = !draft.freeCancellation)
                        }
                        ToggleChip("Free breakfast", draft.freeBreakfast) {
                            draft = draft.copy(freeBreakfast = !draft.freeBreakfast)
                        }
                        ToggleChip("Refundable", draft.refundable) {
                            draft = draft.copy(refundable = !draft.refundable)
                        }
                        ToggleChip("Deals", draft.deals) {
                            draft = draft.copy(deals = !draft.deals)
                        }
                    }
                }

                // Price — per room per night, bounded by what's in the results.
                if (facets.priceMax > facets.priceMin) {
                    FilterSection(title = "Price (per room per night)") {
                        val curMin = draft.minPrice ?: facets.priceMin
                        val curMax = draft.maxPrice ?: facets.priceMax
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RhSpacing.md),
                        ) {
                            PriceBox("Minimum", formatMoney(curMin, facets.currency), Modifier.weight(1f))
                            PriceBox("Maximum", formatMoney(curMax, facets.currency), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(RhSpacing.sm))
                        RangeSlider(
                            value = curMin.toFloat()..curMax.toFloat(),
                            onValueChange = { r ->
                                val lo = r.start.toInt()
                                val hi = r.endInclusive.toInt()
                                draft = draft.copy(
                                    minPrice = if (lo <= facets.priceMin) null else lo,
                                    maxPrice = if (hi >= facets.priceMax) null else hi,
                                )
                            },
                            valueRange = facets.priceMin.toFloat()..facets.priceMax.toFloat(),
                        )
                    }
                }

                // Hotel Star.
                if (facets.stars.isNotEmpty()) {
                    FilterSection(title = "Hotel star") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                        ) {
                            facets.stars.forEach { bucket ->
                                val selected = bucket in draft.stars
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        draft = draft.copy(
                                            stars = if (selected) draft.stars - bucket else draft.stars + bucket,
                                        )
                                    },
                                    label = { Text(if (bucket <= 1) "≤1" else "$bucket") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Star, null, tint = RhStarGold, modifier = Modifier.size(16.dp))
                                    },
                                    shape = RhRadius.button,
                                )
                            }
                        }
                    }
                }

                // Facilities — dynamic from the results, collapsed to a handful by default.
                if (facets.facilities.isNotEmpty()) {
                    FilterSection(title = "Facilities") {
                        val collapsedCount = 8
                        val shown = if (facilitiesExpanded) facets.facilities else facets.facilities.take(collapsedCount)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                        ) {
                            shown.forEach { facility ->
                                val selected = draft.facilities.any { it.equals(facility, ignoreCase = true) }
                                ToggleChip(facility, selected) {
                                    draft = draft.copy(
                                        facilities = if (selected) {
                                            draft.facilities.filterNot { it.equals(facility, ignoreCase = true) }.toSet()
                                        } else {
                                            draft.facilities + facility
                                        },
                                    )
                                }
                            }
                        }
                        val remaining = facets.facilities.size - collapsedCount
                        if (remaining > 0) {
                            TextButton(
                                onClick = { facilitiesExpanded = !facilitiesExpanded },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            ) {
                                Text(if (facilitiesExpanded) "Show less" else "Show $remaining more")
                            }
                        }
                    }
                }

                // Payment method — single choice backed by payAtHotel.
                FilterSection(title = "Payment method") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
                    ) {
                        ToggleChip("Pay at hotel", draft.payment == PaymentFilter.PayAtHotel) {
                            draft = draft.copy(
                                payment = if (draft.payment == PaymentFilter.PayAtHotel) PaymentFilter.Any else PaymentFilter.PayAtHotel,
                            )
                        }
                        ToggleChip("Pay now", draft.payment == PaymentFilter.PayNow) {
                            draft = draft.copy(
                                payment = if (draft.payment == PaymentFilter.PayNow) PaymentFilter.Any else PaymentFilter.PayNow,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(RhSpacing.sm))
            }

            // ── Apply ───────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RhSpacing.lg, vertical = RhSpacing.sm)
                    .navigationBarsPadding(),
            ) {
                RhButton(
                    text = "Show result",
                    onClick = { onApply(draft) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    RhCard(contentPadding = RhSpacing.lg, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(RhSpacing.md))
        content()
    }
}

@Composable
private fun PriceBox(label: String, value: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = RhRadius.field,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = RhSpacing.md, vertical = RhSpacing.sm)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

/** A pill-shaped multi-select chip used across the filter sections. */
@Composable
private fun ToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RhRadius.button,
    )
}
