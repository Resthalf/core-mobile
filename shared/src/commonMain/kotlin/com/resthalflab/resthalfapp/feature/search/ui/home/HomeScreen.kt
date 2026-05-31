package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhBrandTopBar
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.design.components.RhFeatureHighlight
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhInfoBanner
import com.resthalflab.resthalfapp.core.design.components.RhPickerField
import com.resthalflab.resthalfapp.core.design.components.RhSectionHeader
import com.resthalflab.resthalfapp.core.design.components.RhSectionLabel
import com.resthalflab.resthalfapp.core.design.components.RhTextField

@Composable
fun HomeScreen(component: HomeComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        RhBrandTopBar(onActionClick = {})

        Hero()

        RhCard(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
            RhSectionLabel("City")
            Spacer(Modifier.height(RhSpacing.sm))
            RhTextField(
                value = state.destination,
                onValueChange = component::onDestinationChanged,
                placeholder = "Where to?",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.LocationOn,
                trailingIcon = Icons.Outlined.MyLocation,
                onTrailingIconClick = {},
                imeAction = ImeAction.Search,
                onImeAction = component::onSearchClicked,
            )

            Spacer(Modifier.height(RhSpacing.lg))
            RhSectionLabel("Date (night stay)")
            Spacer(Modifier.height(RhSpacing.sm))
            RhPickerField(
                value = state.dateLabel,
                onClick = { /* TODO Phase 3: date picker */ },
                leadingIcon = Icons.Outlined.CalendarMonth,
                trailingIcon = Icons.Outlined.CalendarMonth,
            )

            Spacer(Modifier.height(RhSpacing.lg))
            RhInfoBanner(
                title = state.stayWindowTitle,
                subtitle = state.stayWindowSubtitle,
                leadingIcon = Icons.Outlined.Bedtime,
            )

            Spacer(Modifier.height(RhSpacing.xl))
            RhButton(
                text = "Search Night Rooms",
                onClick = component::onSearchClicked,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(modifier = Modifier.padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
            RhSectionHeader("Why book with RestHalf?")
            Spacer(Modifier.height(RhSpacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm)) {
                RhFeatureHighlight(
                    icon = Icons.Outlined.Schedule,
                    title = "12AM–12PM",
                    subtitle = "fixed window",
                    modifier = Modifier.weight(1f),
                )
                RhFeatureHighlight(
                    icon = Icons.Outlined.Sell,
                    title = "Great prices",
                    subtitle = "guaranteed",
                    modifier = Modifier.weight(1f),
                )
                RhFeatureHighlight(
                    icon = Icons.Outlined.EventAvailable,
                    title = "Free",
                    subtitle = "cancellation",
                    modifier = Modifier.weight(1f),
                )
                RhFeatureHighlight(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Trusted",
                    subtitle = "hotels",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(RhSpacing.xl))
        }
    }
}

@Composable
private fun Hero() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Day rooms for rest, work or layover",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            Text(
                text = "Stay at night, leave by noon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RhIllustrationPlaceholder(
            modifier = Modifier
                .padding(start = RhSpacing.md)
                .size(width = 110.dp, height = 88.dp),
        )
    }
}
