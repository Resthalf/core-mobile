package com.resthalflab.resthalfapp.feature.bookings.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.RhSuccessContainer
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhInfoBanner
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus

private val NavyCard = Color(0xFF1A237E)
private val NavyCardContent = Color(0xFFFFFFFF)
private val NavyCardMuted = Color(0xFFB0B8E8)

@Composable
fun BookingDetailScreen(component: BookingDetailComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val vacate by component.vacate.collectAsStateWithLifecycle()
    val cancel by component.cancel.collectAsStateWithLifecycle()

    when (val s = state) {
        BookingDetailComponent.State.Loading -> LoadingScreen(component::onBackClicked)
        is BookingDetailComponent.State.Error -> ErrorScreen(
            s.message,
            component::onBackClicked,
            component::onRetry
        )

        is BookingDetailComponent.State.Content -> Content(s, vacate, cancel, component)
    }

    // Result of a vacate request — surfaced over whatever content is showing.
    when {
        vacate.successMessage != null -> AlertDialog(
            onDismissRequest = component::onDismissVacateResult,
            title = { Text("Room released") },
            text = { Text(vacate.successMessage!!) },
            confirmButton = { TextButton(onClick = component::onDismissVacateResult) { Text("OK") } },
        )

        vacate.error != null -> AlertDialog(
            onDismissRequest = component::onDismissVacateResult,
            title = { Text("Couldn't release room") },
            text = { Text(vacate.error!!) },
            confirmButton = { TextButton(onClick = component::onDismissVacateResult) { Text("OK") } },
        )
    }
}

@Composable
private fun LoadingScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(RhSpacing.sm)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ErrorScreen(message: String, onBack: () -> Unit, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(RhSpacing.sm)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RhSpacing.md)
        ) {
            Text(message, color = MaterialTheme.colorScheme.error)
            RhButton("Retry", onRetry)
        }
    }
}

@Composable
private fun Content(
    state: BookingDetailComponent.State.Content,
    vacate: BookingDetailComponent.VacateState,
    cancel: BookingDetailComponent.CancelState,
    component: BookingDetailComponent,
) {
    val booking = state.booking
    var showVacateConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        StickySection(booking, state, component::onBackClicked)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            HotelSection(booking)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            QuickActionsRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(RhSpacing.md))
            ImportantBanner()

            if (booking.status == BookingStatus.Pending) {
                Spacer(Modifier.height(RhSpacing.lg))
                RhButton(
                    text = "Proceed to Payment",
                    onClick = component::onProceedToPayment,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg),
                )
            }

            if (booking.status == BookingStatus.Active) {
                Spacer(Modifier.height(RhSpacing.lg))
                RhButton(
                    text = "Confirm to Vacate",
                    onClick = { showVacateConfirm = true },
                    loading = vacate.submitting,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg),
                )
            }

            if (state.canCancel) {
                Spacer(Modifier.height(RhSpacing.lg))
                RhButton(
                    text = "Cancel Reservation",
                    onClick = component::onCancelReservation,
                    loading = cancel.busy,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.lg),
                )
            }

            Spacer(Modifier.height(RhSpacing.lg))
        }
    }

    CancelDialogs(cancel = cancel, currency = booking.currency, component = component)

    if (showVacateConfirm) {
        AlertDialog(
            onDismissRequest = { showVacateConfirm = false },
            title = { Text("Leaving already?") },
            text = { Text("Confirm you've finished your stay and left the room. This releases it for the next guest.") },
            confirmButton = {
                TextButton(onClick = {
                    showVacateConfirm = false
                    component.onConfirmVacate()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVacateConfirm = false
                }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CancelDialogs(
    cancel: BookingDetailComponent.CancelState,
    currency: String,
    component: BookingDetailComponent,
) {
    when {
        cancel.done -> AlertDialog(
            onDismissRequest = component::onDismissCancel,
            title = { Text("Reservation cancelled") },
            text = { Text("Your reservation has been cancelled and any eligible refund is being processed.") },
            confirmButton = { TextButton(onClick = component::onDismissCancel) { Text("OK") } },
        )

        cancel.error != null -> AlertDialog(
            onDismissRequest = component::onDismissCancel,
            title = { Text("Couldn't cancel") },
            text = { Text(cancel.error) },
            confirmButton = { TextButton(onClick = component::onDismissCancel) { Text("OK") } },
        )

        cancel.preview != null -> {
            val preview = cancel.preview
            if (preview.allowed) {
                AlertDialog(
                    onDismissRequest = component::onDismissCancel,
                    title = { Text("Cancel this reservation?") },
                    text = {
                        Column {
                            Text(preview.reason)
                            Spacer(Modifier.height(RhSpacing.sm))
                            Text(
                                text = "Refund: ${formatMoney(preview.refundAmount, currency)} (${preview.refundPercent}%)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = "Policy: ${preview.policyType.replace('_', ' ')}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = component::onConfirmCancel) { Text("Cancel reservation") }
                    },
                    dismissButton = {
                        TextButton(onClick = component::onDismissCancel) { Text("Keep") }
                    },
                )
            } else {
                AlertDialog(
                    onDismissRequest = component::onDismissCancel,
                    title = { Text("Cancellation not allowed") },
                    text = { Text(preview.reason) },
                    confirmButton = { TextButton(onClick = component::onDismissCancel) { Text("OK") } },
                )
            }
        }
    }
}

@Composable
private fun StickySection(
    booking: Booking,
    state: BookingDetailComponent.State.Content,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = RhSpacing.sm,
                    end = RhSpacing.lg,
                    top = RhSpacing.xs,
                    bottom = RhSpacing.xs
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = booking.status.screenTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Headset,
                    contentDescription = "Support",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        StayWindowCard(booking, state)
        Spacer(Modifier.height(RhSpacing.sm))
    }
}

@Composable
private fun StayWindowCard(booking: Booking, state: BookingDetailComponent.State.Content) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RhSpacing.lg),
        shape = RoundedCornerShape(16.dp),
        color = NavyCard,
    ) {
        Column(modifier = Modifier.padding(RhSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Stay Window",
                    style = MaterialTheme.typography.labelMedium,
                    color = NavyCardMuted,
                )
                RhTag(
                    text = booking.status.tagLabel,
                    containerColor = booking.status.tagBg,
                    contentColor = booking.status.tagFg,
                )
            }
            Spacer(Modifier.height(RhSpacing.xs))
            Text(
                text = booking.stayWindow,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = NavyCardContent,
            )

            Spacer(Modifier.height(RhSpacing.md))
            HorizontalDivider(color = NavyCardMuted.copy(alpha = 0.3f))
            Spacer(Modifier.height(RhSpacing.md))

            Text(
                text = if (state.countingToStart) "Starts in" else "Time left until checkout",
                style = MaterialTheme.typography.labelMedium,
                color = NavyCardMuted,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                TimeUnit(value = state.hoursLeft, label = "HOURS")
                Separator()
                TimeUnit(value = state.minutesLeft, label = "MINUTES")
                Separator()
                TimeUnit(value = state.secondsLeft, label = "SECONDS")
            }
            Spacer(Modifier.height(RhSpacing.md))
            LinearProgressIndicator(
                progress = { state.checkoutProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = RhSuccess,
                trackColor = NavyCardMuted.copy(alpha = 0.25f),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun TimeUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
            ),
            color = NavyCardContent,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NavyCardMuted,
        )
    }
}

@Composable
private fun Separator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        ),
        color = NavyCardContent,
        modifier = Modifier.padding(horizontal = RhSpacing.sm).padding(bottom = 12.dp),
    )
}

@Composable
private fun HotelSection(booking: Booking) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(RhSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(width = 88.dp, height = 72.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            RhIllustrationPlaceholder(modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(RhSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = booking.hotelName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = booking.city,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.xl)) {
                InfoPair(label = "Booking ID", value = booking.bookingCode)
                InfoPair(label = "Room", value = "Room ${booking.roomNumber}")
            }
        }
    }
}

@Composable
private fun InfoPair(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickActionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = RhSpacing.lg),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickAction(
            icon = Icons.Outlined.Hotel,
            label = "Hotel Info",
            tint = MaterialTheme.colorScheme.primary
        ) {}
        QuickAction(
            icon = Icons.Outlined.Call,
            label = "Call Hotel",
            tint = MaterialTheme.colorScheme.primary
        ) {}
        QuickAction(icon = Icons.Outlined.Whatsapp, label = "WhatsApp", tint = Color(0xFF25D366)) {}
        QuickAction(
            icon = Icons.Outlined.LocationOn,
            label = "Get Direction",
            tint = MaterialTheme.colorScheme.primary
        ) {}
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RhSpacing.xs),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ImportantBanner() {
    RhInfoBanner(
        subtitle = "• Check-out by 12:00 PM today.\n• Late check-out or overstay is subject to hotel's policy & additional charges.",
        leadingIcon = Icons.Outlined.Schedule,
        title = "Important",
        modifier = Modifier.padding(horizontal = RhSpacing.lg),
    )
}

// ── Status helpers ────────────────────────────────────────────────────────────

private val BookingStatus.screenTitle: String
    get() = when (this) {
        BookingStatus.Pending -> "Pending Payment"
        BookingStatus.Confirmed -> "Upcoming Stay"
        BookingStatus.Active -> "Active Stay"
        BookingStatus.Completed -> "Completed Stay"
        BookingStatus.Cancelled -> "Cancelled Booking"
        BookingStatus.Overstayed -> "Overstayed"
        BookingStatus.InternalError -> "Booking Error"
    }

private val BookingStatus.tagLabel: String
    get() = when (this) {
        BookingStatus.Pending -> "PENDING"
        BookingStatus.Confirmed -> "CONFIRMED"
        BookingStatus.Active -> "ACTIVE"
        BookingStatus.Completed -> "COMPLETED"
        BookingStatus.Cancelled -> "CANCELLED"
        BookingStatus.Overstayed -> "OVERSTAYED"
        BookingStatus.InternalError -> "INTERNAL ERROR"
    }

private val BookingStatus.tagBg: Color
    get() = when (this) {
        BookingStatus.Pending -> Color(0xFFFFE8CC)
        BookingStatus.Confirmed -> Color(0xFFE3F0FF)
        BookingStatus.Active -> RhSuccessContainer
        BookingStatus.Completed -> RhSuccessContainer
        BookingStatus.Cancelled -> Color(0xFFFFE0E0)
        BookingStatus.Overstayed -> Color(0xFFFBEBCB)
        BookingStatus.InternalError -> Color(0xFFFFE0E0)
    }

private val BookingStatus.tagFg: Color
    get() = when (this) {
        BookingStatus.Pending -> Color(0xFF9A5B00)
        BookingStatus.Confirmed -> Color(0xFF1A4E8A)
        BookingStatus.Active -> RhOnSuccessContainer
        BookingStatus.Completed -> RhOnSuccessContainer
        BookingStatus.Cancelled -> Color(0xFFB71C1C)
        BookingStatus.Overstayed -> Color(0xFF6B4E16)
        BookingStatus.InternalError -> Color(0xFFB71C1C)
    }
