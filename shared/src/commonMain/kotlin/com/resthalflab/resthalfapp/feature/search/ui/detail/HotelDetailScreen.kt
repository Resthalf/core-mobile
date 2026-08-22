package com.resthalflab.resthalfapp.feature.search.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KingBed
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhCard
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhRatingRow
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhScrollableTabs
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomOffer
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomRateOption
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SectionKey(val label: String) {
    Overview("General Info"),
    Reviews("Reviews"),
    Facilities("Popular Facilities"),
    Location("Location"),
    Nearby("Nearby Attractions"),
    Rules("Accommodation Rules"),
}

@Composable
fun HotelDetailScreen(component: HotelDetailComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val isFavorite by component.isFavorite.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showAllHighlights by remember { mutableStateOf(false) }
    var showAllFacilities by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            state.loading -> LoadingState(onBack = component::onBackClicked)
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = component::onRetry,
                onBack = component::onBackClicked,
            )

            else -> LoadedContent(
                state = state,
                isFavorite = isFavorite,
                listState = listState,
                onBack = component::onBackClicked,
                onToggleFavorite = component::onToggleFavorite,
                onSelectOption = component::onSelectOption,
                onShowAllHighlights = { showAllHighlights = true },
                onShowAllFacilities = { showAllFacilities = true },
                scrollTo = { index, offsetPx -> scope.launch { listState.animateScrollToItem(index, offsetPx) } },
            )
        }

        if (showAllHighlights) {
            AllItemsDialog("Highlights", state.allHighlights) { showAllHighlights = false }
        }
        if (showAllFacilities) {
            AllItemsDialog("Popular facilities", state.popularFacilities) { showAllFacilities = false }
        }
    }
}

@Composable
private fun LoadedContent(
    state: HotelDetailComponent.UiState,
    isFavorite: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectOption: (RoomOffer, RoomRateOption) -> Unit,
    onShowAllHighlights: () -> Unit,
    onShowAllFacilities: () -> Unit,
    scrollTo: (index: Int, offsetPx: Int) -> Unit,
) {
    val sections = remember(state) { sectionsFor(state) }
    val tabLabels = sections.map { it.label }

    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val overlayOffsetPx = with(density) { (statusTop + 104.dp).roundToPx() }

    // Active list item under the sticky overlay (index 0 = hero, 1..N = tabbed sections, N+1 = Room
    // Details). Uses layoutInfo (not firstVisibleItemIndex) so the negative jump offset doesn't leave
    // the indicator one tab behind. When the reader is in Room Details, no tab is active.
    val activeItemIndex by remember(sections, overlayOffsetPx) {
        derivedStateOf {
            val info = listState.layoutInfo
            val threshold = info.viewportStartOffset + overlayOffsetPx
            (
                info.visibleItemsInfo.lastOrNull { it.index >= 1 && it.offset <= threshold + 1 }
                    ?: info.visibleItemsInfo.firstOrNull { it.index >= 1 }
                )?.index ?: 1
        }
    }
    val selectedTab = (activeItemIndex - 1).coerceIn(0, (sections.size - 1).coerceAtLeast(0))
    val inRoomsSection = activeItemIndex > sections.size
    val collapseAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 350f).coerceIn(0f, 1f)
        }
    }
    val jumpTo: (Int) -> Unit = { sectionIdx -> scrollTo(1 + sectionIdx, -overlayOffsetPx) }
    // The Room Details section is rendered right after the tabbed sections (not a tab of its own).
    val onSeeRooms: () -> Unit = { if (state.rooms.isNotEmpty()) jumpTo(sections.size) }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        item {
            HeroHeaderBlock(
                state = state,
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
            )
        }
        itemsIndexed(sections) { _, key ->
            SectionContent(
                key = key,
                state = state,
                onShowAllHighlights = onShowAllHighlights,
                onShowAllFacilities = onShowAllFacilities,
            )
        }
        if (state.rooms.isNotEmpty()) {
            item { RoomsSection(rooms = state.rooms, onSelect = onSelectOption) }
        }
    }

    if (collapseAlpha > 0.01f && tabLabels.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth().alpha(collapseAlpha),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
        ) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xs, vertical = RhSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BarIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                    Text(
                        text = state.hotelName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = RhSpacing.xs),
                    )
                    BarIconButton(
                        icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        onClick = onToggleFavorite,
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                RhScrollableTabs(
                    tabs = tabLabels,
                    selectedIndex = selectedTab,
                    onTabSelected = jumpTo,
                    showIndicator = !inRoomsSection,
                )
            }
        }
    }

    if (state.rooms.isNotEmpty()) {
        BottomCta(
            fromPrice = state.fromPrice,
            currency = state.currency,
            onSeeRooms = onSeeRooms,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    }
}

private fun sectionsFor(state: HotelDetailComponent.UiState): List<SectionKey> = buildList {
    add(SectionKey.Overview)
    if (state.reviewRating != null || state.reviewCategories.isNotEmpty()) add(SectionKey.Reviews)
    if (state.popularFacilities.isNotEmpty()) add(SectionKey.Facilities)
    if (state.locationAddress != null || (state.geoLat != null && state.geoLong != null)) add(SectionKey.Location)
    if (state.nearbyAttractions.isNotEmpty()) add(SectionKey.Nearby)
    if (state.rules != null) add(SectionKey.Rules)
}

@Composable
private fun SectionContent(
    key: SectionKey,
    state: HotelDetailComponent.UiState,
    onShowAllHighlights: () -> Unit,
    onShowAllFacilities: () -> Unit,
) {
    when (key) {
        SectionKey.Overview -> OverviewSection(state, onShowAllHighlights)
        SectionKey.Reviews -> ReviewsSection(state)
        SectionKey.Facilities -> FacilitiesSection(state, onShowAllFacilities)
        SectionKey.Location -> LocationSection(state)
        SectionKey.Nearby -> NearbySection(state)
        SectionKey.Rules -> RulesSection(state)
    }
}

// ---- Hero + header ------------------------------------------------------------------------------

@Composable
private fun HeroHeaderBlock(
    state: HotelDetailComponent.UiState,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Hero(imageUrl = state.heroImage, isFavorite = isFavorite, onBack = onBack, onToggleFavorite = onToggleFavorite)
        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = RhSpacing.lg, vertical = RhSpacing.lg)) {
                HotelHeader(state)
            }
        }
    }
}

@Composable
private fun HotelHeader(state: HotelDetailComponent.UiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RhTag(text = (state.category ?: "Hotel").uppercase())
            state.starRating?.let { stars ->
                Spacer(Modifier.width(RhSpacing.sm))
                Row {
                    repeat(stars.coerceIn(0, 5)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = RhStarGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(RhSpacing.sm))
        Text(
            text = state.hotelName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        state.reviewRating?.let { rating ->
            Spacer(Modifier.height(RhSpacing.xs))
            RhRatingRow(rating = rating, reviews = state.reviewCount ?: 0, reviewWord = "reviews")
        }
        state.address?.let { address ->
            Spacer(Modifier.height(RhSpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(RhSpacing.xs))
                Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ---- Sections -----------------------------------------------------------------------------------

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg, vertical = RhSpacing.sm)) {
        RhCard(modifier = Modifier.fillMaxWidth(), contentPadding = RhSpacing.lg) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                action?.invoke()
            }
            Spacer(Modifier.height(RhSpacing.md))
            content()
        }
    }
}

@Composable
private fun OverviewSection(
    state: HotelDetailComponent.UiState,
    onShowAllHighlights: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg, vertical = RhSpacing.sm)) {
        RhCard(modifier = Modifier.fillMaxWidth(), contentPadding = RhSpacing.lg) {
            state.aboutText?.let { about ->
                var expanded by remember { mutableStateOf(false) }
                Text(
                    text = "About this property",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(RhSpacing.sm))
                Text(
                    text = about,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                )
                if (about.length > 220) {
                    TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                        Text(if (expanded) "Read less" else "Read more")
                    }
                }
            }

            if (state.highlights.isNotEmpty()) {
                if (state.aboutText != null) {
                    Spacer(Modifier.height(RhSpacing.lg))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(RhSpacing.lg))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Highlights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.allHighlights.size > state.highlights.size) {
                        TextButton(onClick = onShowAllHighlights, contentPadding = PaddingValues(0.dp)) { Text("See all") }
                    }
                }
                Spacer(Modifier.height(RhSpacing.xs))
                state.highlights.forEach { IconLabelRow(it) }
            }
        }
    }
}

@Composable
private fun ReviewsSection(state: HotelDetailComponent.UiState) {
    SectionCard(title = "Guest reviews") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = state.reviewRating?.toString() ?: "—",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                state.reviewRating?.let { StarRow(it) }
            }
            Spacer(Modifier.width(RhSpacing.lg))
            Column {
                state.reviewCount?.let {
                    Text("$it reviews", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.recommendPercent?.let {
                    Text("${it.roundToInt()}% would recommend", style = MaterialTheme.typography.bodySmall, color = RhSuccess)
                }
            }
        }
        if (state.reviewCategories.isNotEmpty()) {
            Spacer(Modifier.height(RhSpacing.md))
            state.reviewCategories.forEach { category ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(120.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    LinearProgressIndicator(
                        progress = { (category.rating / 5f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.width(RhSpacing.sm))
                    Text(
                        text = category.rating.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FacilitiesSection(state: HotelDetailComponent.UiState, onSeeAll: () -> Unit) {
    val preview = state.popularFacilities.take(8)
    SectionCard(
        title = "Popular facilities",
        action = {
            if (state.popularFacilities.size > preview.size) {
                TextButton(onClick = onSeeAll, contentPadding = PaddingValues(0.dp)) { Text("See all") }
            }
        },
    ) {
        preview.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { facility ->
                    Box(modifier = Modifier.weight(1f)) { IconLabelRow(facility) }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LocationSection(state: HotelDetailComponent.UiState) {
    SectionCard(title = "Location") {
        state.locationAddress?.let { address ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(RhSpacing.sm))
                Text(address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (state.geoLat != null && state.geoLong != null) {
            Spacer(Modifier.height(RhSpacing.sm))
            Text(
                text = "Lat, Long: ${state.geoLat}, ${state.geoLong}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.xs))
            Text(
                text = "Map preview coming soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NearbySection(state: HotelDetailComponent.UiState) {
    SectionCard(title = "Nearby attractions") {
        state.nearbyAttractions.forEach { attraction ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(RhSpacing.sm))
                    Text(attraction.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                attraction.distanceLabel?.let {
                    Spacer(Modifier.width(RhSpacing.sm))
                    Text(it, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RulesSection(state: HotelDetailComponent.UiState) {
    val rules = state.rules ?: return
    SectionCard(title = "Accommodation rules") {
        if (rules.checkInTime != null || rules.checkOutTime != null) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(RhSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Check-in", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(rules.checkInTime ?: "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Check-out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(rules.checkOutTime ?: "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        rules.policies.forEach { policy ->
            Spacer(Modifier.height(RhSpacing.md))
            Text(policy.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(RhSpacing.xs))
            Text(policy.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RoomsSection(rooms: List<RoomOffer>, onSelect: (RoomOffer, RoomRateOption) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg, vertical = RhSpacing.sm)) {
        Text(
            text = "Room Details",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(RhSpacing.md))
        rooms.forEach { room ->
            RoomCard(room = room, onSelect = onSelect)
            Spacer(Modifier.height(RhSpacing.md))
        }
    }
}

@Composable
private fun RoomImageCarousel(images: List<String>, contentDescription: String?) {
    val shape = RoundedCornerShape(12.dp)
    if (images.size == 1) {
        RhRemoteImage(
            url = images[0],
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(shape),
        )
        return
    }
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(shape)) {
        HorizontalPager(state = pagerState, modifier = Modifier.matchParentSize()) { page ->
            RhRemoteImage(url = images[page], contentDescription = contentDescription, modifier = Modifier.fillMaxSize())
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(RhSpacing.sm),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.55f),
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/${images.size}",
                modifier = Modifier.padding(horizontal = RhSpacing.sm, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomCard(room: RoomOffer, onSelect: (RoomOffer, RoomRateOption) -> Unit) {
    RhCard(modifier = Modifier.fillMaxWidth(), contentPadding = RhSpacing.md) {
        if (room.images.isNotEmpty()) {
            RoomImageCarousel(images = room.images, contentDescription = room.name)
            Spacer(Modifier.height(RhSpacing.sm))
        }
        Text(
            text = room.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(RhSpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            room.bedInfo?.let { bed ->
                Icon(Icons.Outlined.KingBed, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(RhSpacing.xs))
                Text(bed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            room.maxGuests?.let { guests ->
                if (room.bedInfo != null) Spacer(Modifier.width(RhSpacing.md))
                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(RhSpacing.xs))
                Text("Up to $guests", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (room.facilities.isNotEmpty()) {
            Spacer(Modifier.height(RhSpacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs), verticalArrangement = Arrangement.spacedBy(RhSpacing.xs)) {
                room.facilities.take(4).forEach { facility ->
                    RhTag(text = facility, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        room.options.forEach { option ->
            Spacer(Modifier.height(RhSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(RhSpacing.md))
            RateOptionRow(option = option, onSelect = { onSelect(room, option) })
        }
    }
}

@Composable
private fun RateOptionRow(option: RoomRateOption, onSelect: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (option.breakfastIncluded) Icons.Filled.Restaurant else Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (option.breakfastIncluded) RhSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(RhSpacing.xs))
                Text(
                    text = option.boardBasisLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (option.breakfastIncluded) RhSuccess else MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(RhSpacing.xs))
            Text(
                text = if (option.refundable) "Free cancellation" else "Non-refundable",
                style = MaterialTheme.typography.labelMedium,
                color = if (option.refundable) RhSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            Text(
                text = "${formatMoney(option.perNightRate, option.currency)} / night",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${formatMoney(option.totalRate, option.currency)} total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(RhSpacing.md))
        Button(
            onClick = onSelect,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = RhSpacing.lg, vertical = RhSpacing.xs),
        ) {
            Text("Select", style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ---- Shared bits --------------------------------------------------------------------------------

@Composable
private fun IconLabelRow(name: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = iconForFacility(name), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(RhSpacing.md))
        Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StarRow(rating: Double) {
    val filled = rating.roundToInt().coerceIn(0, 5)
    Row {
        repeat(5) { index ->
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < filled) RhStarGold else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun Hero(imageUrl: String?, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        if (imageUrl != null) {
            RhRemoteImage(url = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else {
            RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
        }
        ScrimIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding(),
            onClick = onBack,
        )
        Row(modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding()) {
            ScrimIconButton(icon = Icons.Filled.Share, contentDescription = "Share", onClick = { })
            ScrimIconButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Save",
                onClick = onToggleFavorite,
            )
        }
    }
}

@Composable
private fun ScrimIconButton(icon: ImageVector, contentDescription: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.padding(RhSpacing.sm).size(40.dp), shape = CircleShape, color = Color.Black.copy(alpha = 0.35f)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun BarIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(onClick = onClick, modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.Transparent) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun BottomCta(fromPrice: Int?, currency: String, onSeeRooms: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = RhSpacing.lg, vertical = RhSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Starts from", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = fromPrice?.let { "${formatMoney(it, currency)} / night" } ?: "—",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            RhButton(text = "See rooms", onClick = onSeeRooms)
        }
    }
}

@Composable
private fun AllItemsDialog(title: String, items: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                items.forEach { IconLabelRow(it) }
            }
        },
    )
}

@Composable
private fun LoadingState(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScrimIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.align(Alignment.TopStart).statusBarsPadding(), onBack)
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(RhSpacing.md)) {
            CircularProgressIndicator()
            Text("Loading hotel…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScrimIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.align(Alignment.TopStart).statusBarsPadding(), onBack)
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = RhSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
        ) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RhButton(text = "Retry", onClick = onRetry)
        }
    }
}

/** Best-effort icon for a facility/highlight label; falls back to a check mark. */
private fun iconForFacility(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "wifi" in n || "internet" in n -> Icons.Filled.Wifi
        "parking" in n -> Icons.Filled.LocalParking
        "breakfast" in n || "food" in n || "dining" in n || "restaurant" in n -> Icons.Filled.Restaurant
        "pool" in n || "swimming" in n -> Icons.Filled.Pool
        "fitness" in n || "gym" in n -> Icons.Filled.FitnessCenter
        "spa" in n || "sauna" in n -> Icons.Filled.Spa
        "bar" in n || "lounge" in n -> Icons.Filled.LocalBar
        "airport" in n || "shuttle" in n -> Icons.Filled.AirportShuttle
        "elevator" in n -> Icons.Filled.Elevator
        "laundry" in n || "cleaning" in n -> Icons.Filled.LocalLaundryService
        "business" in n || "meeting" in n || "banquet" in n || "conference" in n -> Icons.Filled.BusinessCenter
        "concierge" in n || "guest service" in n || "front desk" in n -> Icons.Filled.SupportAgent
        "room service" in n -> Icons.Filled.RoomService
        "coffee" in n -> Icons.Filled.Coffee
        "garden" in n || "things to do" in n || "terrace" in n -> Icons.Filled.Deck
        "television" in n || "tv" in n -> Icons.Filled.Tv
        "accessible" in n || "disability" in n || "wheelchair" in n -> Icons.AutoMirrored.Filled.Accessible
        "multilingual" in n || "language" in n -> Icons.Filled.Language
        "gift" in n || "shop" in n -> Icons.Filled.CardGiftcard
        "hair" in n -> Icons.Filled.Checkroom
        else -> Icons.Filled.Check
    }
}
