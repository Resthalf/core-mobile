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
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhBrandTopBar
import com.resthalflab.resthalfapp.core.design.components.RhFeatureHighlight
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhSectionHeader

@Composable
fun HomeScreen(component: HomeComponent) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        RhBrandTopBar(onActionClick = {})

        Hero()

        SearchPlanner(
            component = component,
            searchButtonText = "Search Hotels",
        )

        WhyBook()
    }
}

@Composable
private fun Hero() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md),
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
            modifier = Modifier.padding(start = RhSpacing.md).size(width = 110.dp, height = 88.dp),
        )
    }
}

@Composable
private fun WhyBook() {
    Column(modifier = Modifier.padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md)) {
        RhSectionHeader("Why book with RestHalf?")
        Spacer(Modifier.height(RhSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm)) {
            RhFeatureHighlight(Icons.Outlined.Schedule, "12AM–12PM", "fixed window", Modifier.weight(1f))
            RhFeatureHighlight(Icons.Outlined.Sell, "Great prices", "guaranteed", Modifier.weight(1f))
            RhFeatureHighlight(Icons.Outlined.EventAvailable, "Free", "cancellation", Modifier.weight(1f))
            RhFeatureHighlight(Icons.Outlined.VerifiedUser, "Trusted", "hotels", Modifier.weight(1f))
        }
        Spacer(Modifier.height(RhSpacing.xl))
    }
}
