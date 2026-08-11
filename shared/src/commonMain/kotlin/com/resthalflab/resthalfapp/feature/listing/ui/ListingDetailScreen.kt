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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhSectionLabel
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent

@Composable
fun ListingDetailScreen(component: ListingDetailComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Hero(photoUrls = state.photoUrls, onBack = component::onBackClicked)

        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = RhSpacing.lg).navigationBarsPadding()) {
                Spacer(Modifier.height(RhSpacing.lg))

                // Hotel title: name + city only.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.hotelName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = state.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    RhTag(
                        text = "Free cancellation",
                        containerColor = RhSuccessContainer,
                        contentColor = RhOnSuccessContainer,
                    )
                }

                // Stay Window section — carried from the search choice.
                Spacer(Modifier.height(RhSpacing.lg))
                RhInfoBanner(
                    title = state.stayTitle,
                    subtitle = state.stayWindowLine,
                    caption = state.checkInOutLine,
                    leadingIcon = Icons.Outlined.Schedule,
                )

                // Room Type section — shows the picked room (number + id).
                Spacer(Modifier.height(RhSpacing.lg))
                RhSectionLabel("Room Type", uppercase = false)
                Text(
                    text = "Room ${state.roomNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "ID: ${state.roomId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(RhSpacing.lg))
                RhSectionLabel("Cancellation Policy", uppercase = false)
                Text(
                    text = "Free cancellation before check-in time.",
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
                PriceRow("Room Price", state.priceLabel)
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
                        text = state.priceLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                state.error?.let {
                    Spacer(Modifier.height(RhSpacing.sm))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(RhSpacing.xl))
                RhButton(
                    text = state.bookButtonText,
                    onClick = component::onBookClicked,
                    loading = state.submitting,
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
                modifier = Modifier.align(Alignment.BottomEnd).padding(RhSpacing.md, RhSpacing.xl),
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
private fun PriceRow(label: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
