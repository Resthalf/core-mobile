package com.resthalflab.resthalfapp.feature.listing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccessContainer
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhInfoBanner
import com.resthalflab.resthalfapp.core.design.components.RhRatingRow
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhSectionLabel
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent.State

@Composable
fun ListingDetailScreen(component: ListingDetailComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    when (val s = state) {
        State.Loading -> WithBack(component::onBackClicked) { CircularProgressIndicator() }
        is State.Error -> WithBack(component::onBackClicked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RhSpacing.md),
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                RhButton(text = "Retry", onClick = component::onRetry)
            }
        }
        is State.Content -> DetailContent(s.detail, component::onBackClicked)
    }
}

@Composable
private fun WithBack(onBack: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(RhSpacing.sm),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Box(modifier = Modifier.align(Alignment.Center)) { content() }
    }
}

@Composable
private fun DetailContent(detail: ListingDetail, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Hero(photoUrls = detail.photoUrls, onBack = onBack)

        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = RhSpacing.lg).navigationBarsPadding()) {
                Spacer(Modifier.height(RhSpacing.lg))
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = detail.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(RhSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RhRatingRow(rating = detail.rating, reviews = detail.reviewsCount, reviewWord = "reviews")
                    RhTag(
                        text = "Free cancellation",
                        containerColor = RhSuccessContainer,
                        contentColor = RhOnSuccessContainer,
                    )
                }

                Spacer(Modifier.height(RhSpacing.lg))
                RhInfoBanner(
                    title = "Stay Window (Fixed)",
                    subtitle = "12:00 AM – 12:00 PM (12 hours)",
                    caption = "Check-in at 12:00 AM, check-out by 12:00 PM",
                    leadingIcon = Icons.Outlined.Schedule,
                )

                Spacer(Modifier.height(RhSpacing.lg))
                RhSectionLabel("Room Type", uppercase = false)
                Text(
                    text = detail.roomType,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(RhSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.lg)) {
                    Attribute(Icons.Outlined.People, "${detail.guests} Guests")
                    Attribute(Icons.Outlined.Bed, detail.bedType)
                    Attribute(Icons.Outlined.SquareFoot, "${detail.areaSqm} m²")
                }

                Spacer(Modifier.height(RhSpacing.lg))
                RhSectionLabel("Cancellation Policy", uppercase = false)
                Text(
                    text = detail.cancellationPolicy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(RhSpacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(RhSpacing.lg))

                Text(
                    text = "Price Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(RhSpacing.sm))
                PriceRow("Room Price", formatMoney(detail.pricePerNight, detail.currency))
                Spacer(Modifier.height(RhSpacing.xs))
                PriceRow("Service Fee", formatMoney(detail.serviceFee, detail.currency))
                Spacer(Modifier.height(RhSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Total (incl. tax)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = formatMoney(detail.totalPrice, detail.currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(RhSpacing.xl))
                RhButton(
                    text = "Book Night Stay",
                    onClick = { /* TODO Phase 3: booking flow */ },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(RhSpacing.lg))
            }
        }
    }
}

@Composable
private fun Hero(photoUrls: List<String>, onBack: () -> Unit) {
    val pageCount = photoUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        if (photoUrls.isEmpty()) {
            RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.matchParentSize()) { page ->
                RhRemoteImage(
                    url = photoUrls[page],
                    contentDescription = "Room photo ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        ScrimIconButton(
            Icons.AutoMirrored.Filled.ArrowBack,
            "Back",
            Modifier.align(Alignment.TopStart).statusBarsPadding(),
            onBack,
        )
        ScrimIconButton(
            Icons.Outlined.FavoriteBorder,
            "Save",
            Modifier.align(Alignment.TopEnd).statusBarsPadding(),
        ) { }

        if (photoUrls.isNotEmpty()) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(RhSpacing.md,RhSpacing.xl),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / $pageCount",
                    modifier = Modifier.padding(horizontal = RhSpacing.md, vertical = RhSpacing.xs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ScrimIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(RhSpacing.sm).size(40.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun Attribute(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(RhSpacing.xs))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PriceRow(label: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
