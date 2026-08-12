package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.design.components.RhTextButton
import com.resthalflab.resthalfapp.core.design.components.RhTextField
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationType

/** Popular fallbacks shown before the user types. Half-Day (city-based) inventory lives in these. */
private val PopularDestinations = listOf(
    "Jakarta", "Bali", "Bandung", "Surabaya", "Yogyakarta", "Medan",
    "Semarang", "Malang", "Makassar", "Batam", "Palembang", "Bogor",
)

/**
 * "Going Anywhere?" location picker. Opens full-height; debounced autosuggest is driven by the
 * component and the visible results are already filtered for the active slot (Half-Day → cities only).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LocationSearchSheet(
    component: HomeComponent,
    onDismiss: () -> Unit,
) {
    val state by component.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        // fillMaxHeight makes the sheet open at full height rather than wrapping its content.
        Column(modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
                Text(
                    text = "Going anywhere?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = RhSpacing.sm),
                )
            }

            Spacer(Modifier.height(RhSpacing.sm))
            RhTextField(
                value = state.locationQuery,
                onValueChange = component::onLocationQueryChanged,
                placeholder = "Accommodation name, destination, etc.",
                leadingIcon = Icons.Outlined.Search,
                trailingIcon = if (state.locationQuery.isNotEmpty()) Icons.Outlined.Close else null,
                onTrailingIconClick = { component.onLocationQueryChanged("") },
                imeAction = ImeAction.Search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RhSpacing.lg)
                    .focusRequester(focusRequester),
            )

            Spacer(Modifier.height(RhSpacing.md))

            when {
                state.locationQuery.isBlank() -> PopularDestinationsSection(
                    onSelect = { city ->
                        component.onCitySelected(city)
                        onDismiss()
                    },
                )

                state.isSearchingLocation -> CenteredBox(Modifier.weight(1f)) { CircularProgressIndicator() }

                state.locationError -> CenteredBox(Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Couldn't load suggestions", style = MaterialTheme.typography.bodyMedium)
                        RhTextButton(
                            text = "Retry",
                            onClick = { component.onLocationQueryChanged(state.locationQuery) },
                        )
                    }
                }

                state.locationResults.isEmpty() -> CenteredBox(Modifier.weight(1f)) {
                    Text(
                        text = "No matches for \"${state.locationQuery}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.locationResults, key = { it.id }) { suggestion ->
                        LocationSuggestionRow(
                            suggestion = suggestion,
                            onClick = {
                                component.onLocationSelected(suggestion)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopularDestinationsSection(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg)) {
        Text(
            text = "Popular destinations",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(RhSpacing.md))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm)) {
            PopularDestinations.forEach { city ->
                SuggestionChip(onClick = { onSelect(city) }, label = { Text(city) })
            }
        }
    }
}

@Composable
private fun LocationSuggestionRow(suggestion: LocationSuggestion, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(suggestion.name) },
        supportingContent = {
            Text(
                text = suggestion.fullName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = { Icon(suggestion.type.icon(), contentDescription = null) },
        trailingContent = {
            RhTag(
                text = suggestion.type.label(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

private fun LocationType.icon(): ImageVector = when (this) {
    LocationType.CITY -> Icons.Outlined.LocationCity
    LocationType.STATE -> Icons.Outlined.Map
    LocationType.AIRPORT -> Icons.Outlined.Flight
    LocationType.HOTEL -> Icons.Outlined.Apartment
    LocationType.AREA -> Icons.Outlined.Place
    LocationType.UNKNOWN -> Icons.Outlined.Place
}

private fun LocationType.label(): String = when (this) {
    LocationType.CITY -> "City"
    LocationType.STATE -> "State"
    LocationType.AIRPORT -> "Airport"
    LocationType.HOTEL -> "Hotel"
    LocationType.AREA -> "Area"
    LocationType.UNKNOWN -> "Place"
}
