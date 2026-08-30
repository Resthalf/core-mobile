package com.resthalflab.resthalfapp.feature.search.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HomeScreen(component: HomeComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Brand header: the search card sits in front and overhangs the coloured band below it.
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                GreetingRow(userName = state.userName, avatarUrl = state.avatarUrl)
                SearchPlanner(component = component, searchButtonText = "Search")
            }
        }

        Spacer(Modifier.height(RhSpacing.lg))
        PopularHotels()
        Spacer(Modifier.height(RhSpacing.xxl))
    }
}

@Composable
private fun GreetingRow(userName: String, avatarUrl: String?) {
    val firstName = userName.trim().substringBefore(' ')
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = RhSpacing.xl, end = RhSpacing.xl, top = RhSpacing.lg, bottom = RhSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (firstName.isBlank()) "Hello!" else "Hello, $firstName!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Let's start exploring 👋",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Avatar(avatarUrl = avatarUrl, firstName = firstName)
    }
}

@Composable
private fun Avatar(avatarUrl: String?, firstName: String) {
    // Real Google photo when available; otherwise a stable online avatar keyed by name (guests too).
    val seed = firstName.lowercase().ifBlank { "guest" }
    val url = avatarUrl?.takeIf { it.isNotBlank() } ?: "https://i.pravatar.cc/150?u=$seed"
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)),
    ) {
        RhRemoteImage(url = url, contentDescription = "Profile", modifier = Modifier.matchParentSize())
    }
}

@Composable
private fun PopularHotels() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Popular Hotel",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(RhSpacing.md))
        LazyRow(
            contentPadding = PaddingValues(horizontal = RhSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(RhSpacing.md),
        ) {
            items(DUMMY_POPULAR) { hotel -> PopularHotelCard(hotel) }
        }
    }
}

@Composable
private fun PopularHotelCard(hotel: PopularHotel) {
    Column(modifier = Modifier.width(240.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp))) {
            RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(RhSpacing.sm),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = RhStarGold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(hotel.rating.toString(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
        Spacer(Modifier.height(RhSpacing.sm))
        Text(
            text = hotel.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(RhSpacing.xs))
            Text(
                text = hotel.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(RhSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatMoney(hotel.price, "IDR"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = " /night",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RhTag(
                text = "${hotel.roomsLeft} left",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Placeholder content for the Popular Hotel rail — swapped for a real (cached) source later.
private data class PopularHotel(
    val name: String,
    val location: String,
    val price: Int,
    val rating: Double,
    val roomsLeft: Int,
)

private val DUMMY_POPULAR = listOf(
    PopularHotel("The Karma Villa", "Kuta, Badung, Bali", 690_000, 4.8, 2),
    PopularHotel("Emeralda Deluxe Suite", "Kuta, Badung, Bali", 880_000, 4.6, 3),
    PopularHotel("Sunset Beach Resort", "Seminyak, Bali", 1_250_000, 4.9, 1),
    PopularHotel("Ubud Green Retreat", "Ubud, Gianyar, Bali", 540_000, 4.7, 5),
)
