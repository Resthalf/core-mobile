package com.resthalflab.resthalfapp.feature.search.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.KingBed
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhOnSuccessContainer
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.RhSuccess
import com.resthalflab.resthalfapp.core.design.RhSuccessContainer
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhIllustrationPlaceholder
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedCard
import com.resthalflab.resthalfapp.core.design.components.RhRemoteImage
import com.resthalflab.resthalfapp.core.design.components.RhTag
import com.resthalflab.resthalfapp.core.design.components.RhTextField
import com.resthalflab.resthalfapp.core.design.components.RhWebView
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.wholesale.api.BookingResult
import com.resthalflab.resthalfapp.feature.wholesale.api.CardForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestType

private val TITLES = listOf("Mr", "Mrs", "Ms")
private val STEP_LABELS = listOf("Review", "Payment", "Confirm")

@Composable
fun CheckoutScreen(component: CheckoutComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val result = state.bookingResult
        if (result != null) {
            SuccessScreen(state = state, result = result, onDone = component::onDone)
            return@Box
        }
        if (state.showPaymentWebView) {
            PaymentWebViewScreen(state = state, component = component)
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            CheckoutTopBar(onBack = component::onBackClicked)
            when {
                state.priceChecking -> CenteredLoading("Checking availability…")
                state.priceLoadError != null -> PriceErrorState(
                    message = state.priceLoadError!!,
                    onRetry = component::onRetryPriceCheck,
                )

                else -> {
                    StepIndicator(currentStep = state.step)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = RhSpacing.lg),
                    ) {
                        Spacer(Modifier.height(RhSpacing.sm))
                        TripSummaryCard(state)
                        Spacer(Modifier.height(RhSpacing.lg))
                        when (state.step) {
                            CheckoutComponent.STEP_REVIEW -> ReviewStep(state, component)
                            CheckoutComponent.STEP_PAYMENT -> PaymentStep(state, component)
                            else -> ConfirmStep(state)
                        }
                        Spacer(Modifier.height(RhSpacing.lg))
                    }
                    CheckoutBottomBar(state = state, component = component)
                }
            }
        }

        if (state.processing) {
            ProcessingOverlay(label = state.processingLabel ?: "Processing…")
        }
        state.priceChange?.let { change ->
            PriceChangeDialog(
                change = change,
                onConfirm = component::onConfirmPriceChange,
                onDecline = component::onDeclinePriceChange,
            )
        }
        state.error?.let { message ->
            AlertDialog(
                onDismissRequest = component::onDismissError,
                confirmButton = { TextButton(onClick = component::onDismissError) { Text("OK") } },
                title = { Text("Booking failed") },
                text = { Text(message) },
            )
        }
    }
}

// ---- Scaffolding --------------------------------------------------------------------------------

@Composable
private fun CheckoutTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = RhSpacing.xs, horizontal = RhSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Spacer(Modifier.width(RhSpacing.xs))
            Text(
                text = "Secure Checkout",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl, vertical = RhSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        STEP_LABELS.forEachIndexed { index, label ->
            val done = index < currentStep
            val active = index == currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape).background(
                        if (done || active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(RhSpacing.xs))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (index < STEP_LABELS.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f).padding(horizontal = RhSpacing.sm),
                    color = if (currentStep > index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(state: CheckoutComponent.UiState, component: CheckoutComponent) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(RhSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatMoney(state.totalRate, state.currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(RhSpacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RhSpacing.md)) {
                if (state.step > CheckoutComponent.STEP_REVIEW) {
                    RhOutlinedButton(text = "Back", onClick = component::onPrevStep, modifier = Modifier.weight(1f))
                }
                if (state.step == CheckoutComponent.STEP_CONFIRM) {
                    RhButton(
                        text = "Confirm & Pay",
                        onClick = component::onConfirmAndPay,
                        loading = state.processing,
                        modifier = Modifier.weight(1.6f),
                    )
                } else {
                    RhButton(text = "Next", onClick = component::onNextStep, modifier = Modifier.weight(1.6f))
                }
            }
        }
    }
}

// ---- Trip summary -------------------------------------------------------------------------------

@Composable
private fun TripSummaryCard(state: CheckoutComponent.UiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                if (state.heroImage != null) {
                    RhRemoteImage(url = state.heroImage, contentDescription = state.hotelName, modifier = Modifier.fillMaxSize())
                } else {
                    RhIllustrationPlaceholder(modifier = Modifier.matchParentSize())
                }
                Box(
                    modifier = Modifier.matchParentSize().background(
                        Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.6f)),
                    ),
                )
                RhTag(
                    text = if (state.nights == 1) "1 night" else "${state.nights} nights",
                    modifier = Modifier.align(Alignment.TopStart).padding(RhSpacing.sm),
                    containerColor = Color.Black.copy(alpha = 0.55f),
                    contentColor = Color.White,
                )
                Text(
                    text = state.hotelName,
                    modifier = Modifier.align(Alignment.BottomStart).padding(RhSpacing.md),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(modifier = Modifier.padding(RhSpacing.md)) {
                SummaryRow(Icons.Filled.CalendarMonth, "Dates", state.dateLabel)
                SummaryRow(Icons.Outlined.Person, "Guests", state.guestsLabel.ifBlank { "—" })
                SummaryRow(Icons.Outlined.KingBed, "Room", state.roomName)

                Spacer(Modifier.height(RhSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(RhSpacing.xs)) {
                    RhTag(
                        text = state.boardBasisLabel,
                        containerColor = if (state.breakfastIncluded) RhSuccessContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (state.breakfastIncluded) RhOnSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    RhTag(
                        text = if (state.refundable) "Free cancellation" else "Non-refundable",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(RhSpacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(RhSpacing.md))
                PriceRow(
                    label = "${formatMoney(state.perNightRate, state.currency)} x ${state.nights} night${if (state.nights == 1) "" else "s"}",
                    value = formatMoney(state.perNightRate * state.nights, state.currency),
                )
                Spacer(Modifier.height(RhSpacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = formatMoney(state.totalRate, state.currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "Includes all taxes and fees",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(RhSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ---- Step 1: Review (guest details) -------------------------------------------------------------

@Composable
private fun ReviewStep(state: CheckoutComponent.UiState, component: CheckoutComponent) {
    val show = state.showErrors
    SectionTitle("Guest details")
    state.guests.forEachIndexed { index, guest ->
        Spacer(Modifier.height(RhSpacing.sm))
        Text(
            text = if (index == 0) "Guest 1 (lead)" else "Guest ${index + 1}" +
                if (guest.type == GuestType.Child) " (child)" else "",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(RhSpacing.xs))
        Row(verticalAlignment = Alignment.Top) {
            TitleDropdown(
                value = guest.title,
                onSelect = { component.onGuestChange(index, guest.copy(title = it)) },
                modifier = Modifier.width(108.dp),
            )
            Spacer(Modifier.width(RhSpacing.sm))
            RhTextField(
                value = guest.firstName,
                onValueChange = { component.onGuestChange(index, guest.copy(firstName = it)) },
                label = "First name",
                isError = show && guest.firstName.isBlank(),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(RhSpacing.sm))
        RhTextField(
            value = guest.lastName,
            onValueChange = { component.onGuestChange(index, guest.copy(lastName = it)) },
            label = "Last name",
            isError = show && guest.lastName.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(Modifier.height(RhSpacing.lg))
    SectionTitle("Contact & billing")
    Spacer(Modifier.height(RhSpacing.sm))
    val contact = state.contact
    RhTextField(
        value = contact.email,
        onValueChange = { component.onContactChange(contact.copy(email = it)) },
        label = "Email",
        keyboardType = KeyboardType.Email,
        isError = show && !contact.email.contains('@'),
        errorText = if (show && !contact.email.contains('@')) "Enter a valid email" else null,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(RhSpacing.sm))
    RhTextField(
        value = contact.phone,
        onValueChange = { component.onContactChange(contact.copy(phone = it)) },
        label = "Phone",
        keyboardType = KeyboardType.Phone,
        isError = show && contact.phone.isBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(RhSpacing.sm))
    RhTextField(
        value = contact.addressLine1,
        onValueChange = { component.onContactChange(contact.copy(addressLine1 = it)) },
        label = "Address (optional)",
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(RhSpacing.sm))
    Row {
        RhTextField(
            value = contact.city,
            onValueChange = { component.onContactChange(contact.copy(city = it)) },
            label = "City (optional)",
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(RhSpacing.sm))
        RhTextField(
            value = contact.postalCode,
            onValueChange = { component.onContactChange(contact.copy(postalCode = it)) },
            label = "Postal (optional)",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.height(RhSpacing.lg))
    SectionTitle("Special requests")
    Spacer(Modifier.height(RhSpacing.sm))
    RhTextField(
        value = state.specialRequests,
        onValueChange = component::onSpecialRequestsChange,
        label = "e.g. early check-in (optional)",
        singleLine = false,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ---- Step 2: Payment ----------------------------------------------------------------------------

@Composable
private fun PaymentStep(state: CheckoutComponent.UiState, component: CheckoutComponent) {
    val show = state.showErrors
    val card = state.card
    SectionTitle("Payment method")
    Spacer(Modifier.height(RhSpacing.sm))
    RhOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(RhSpacing.sm))
            Text("Credit / debit card (Visa)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
    Spacer(Modifier.height(RhSpacing.md))
    RhTextField(
        value = card.number,
        onValueChange = { component.onCardChange(card.copy(number = it.filter { ch -> ch.isDigit() }.take(19))) },
        label = "Card number",
        placeholder = "4111 1111 1111 1111",
        keyboardType = KeyboardType.Number,
        isError = show && card.number.filter { it.isDigit() }.length !in 13..19,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(RhSpacing.sm))
    RhTextField(
        value = card.nameOnCard,
        onValueChange = { component.onCardChange(card.copy(nameOnCard = it)) },
        label = "Name on card",
        isError = show && card.nameOnCard.isBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(RhSpacing.sm))
    Row {
        RhTextField(
            value = card.expiryMonth,
            onValueChange = { component.onCardChange(card.copy(expiryMonth = it.filter { ch -> ch.isDigit() }.take(2))) },
            label = "MM",
            keyboardType = KeyboardType.Number,
            isError = show && (card.expiryMonth.toIntOrNull() ?: 0) !in 1..12,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(RhSpacing.sm))
        RhTextField(
            value = card.expiryYear,
            onValueChange = { component.onCardChange(card.copy(expiryYear = it.filter { ch -> ch.isDigit() }.take(4))) },
            label = "YYYY",
            keyboardType = KeyboardType.Number,
            isError = show && card.expiryYear.length != 4,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(RhSpacing.sm))
        RhTextField(
            value = card.cvv,
            onValueChange = { component.onCardChange(card.copy(cvv = it.filter { ch -> ch.isDigit() }.take(4))) },
            label = "CVV",
            keyboardType = KeyboardType.Number,
            isError = show && card.cvv.length !in 3..4,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(RhSpacing.md))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(RhSpacing.xs))
        Text(
            "Sandbox — no real charge is made.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---- Step 3: Confirm ----------------------------------------------------------------------------

@Composable
private fun ConfirmStep(state: CheckoutComponent.UiState) {
    SectionTitle("Review & confirm")
    Spacer(Modifier.height(RhSpacing.sm))
    RhOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        state.guests.forEachIndexed { index, guest ->
            DetailRow(
                label = if (index == 0) "Lead guest" else "Guest ${index + 1}",
                value = "${guest.title} ${guest.firstName} ${guest.lastName}".trim(),
            )
        }
        DetailRow("Email", state.contact.email)
        DetailRow("Phone", state.contact.phone)
        DetailRow("Board", state.boardBasisLabel)
        DetailRow("Payment", cardSummary(state.card))
        Spacer(Modifier.height(RhSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(RhSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = formatMoney(state.totalRate, state.currency),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    Spacer(Modifier.height(RhSpacing.md))
    Text(
        text = "By confirming, you agree to our Terms of Service and the property's cancellation policy.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ---- Payment (WebView gateway — Midtrans placeholder) -------------------------------------------

@Composable
private fun PaymentWebViewScreen(state: CheckoutComponent.UiState, component: CheckoutComponent) {
    Column(modifier = Modifier.fillMaxSize()) {
        CheckoutTopBar(onBack = component::onCancelPayment)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        RhWebView(url = state.paymentUrl, modifier = Modifier.weight(1f).fillMaxWidth())
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(RhSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(RhSpacing.xs))
                Text(
                    "Sandbox — tap to simulate a successful payment.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(RhSpacing.sm))
            RhButton(
                text = "Simulate Successful Payment",
                onClick = component::onSimulatePayment,
                loading = state.processing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ---- Success ------------------------------------------------------------------------------------

@Composable
private fun SuccessScreen(state: CheckoutComponent.UiState, result: BookingResult, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = RhSpacing.lg)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(RhSpacing.xxl))
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(RhSuccess),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(RhSpacing.lg))
        Text(
            text = if (result.status.equals("Confirmed", ignoreCase = true)) "Booking Confirmed" else "Booking ${result.status}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(RhSpacing.xs))
        Text(
            text = "Your stay at ${state.hotelName} is booked.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(RhSpacing.lg))
        RhOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            DetailRow("Booking ID", result.bookingId)
            result.hotelConfirmationNumber?.let { DetailRow("Hotel confirmation", it) }
            result.providerConfirmationNumber?.let { DetailRow("Provider confirmation", it) }
            DetailRow("Hotel", state.hotelName)
            DetailRow("Room", state.roomName)
            DetailRow("Dates", state.dateLabel)
            DetailRow("Guests", state.guestsLabel)
            Spacer(Modifier.height(RhSpacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(RhSpacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total paid", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = formatMoney(state.totalRate, state.currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(RhSpacing.xl))
        RhButton(text = "Done", onClick = onDone, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(RhSpacing.lg))
    }
}

// ---- Shared pieces ------------------------------------------------------------------------------

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = RhSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(RhSpacing.md))
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun TitleDropdown(value: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        // A read-only text field so it matches the adjacent name fields exactly (same label + height).
        RhTextField(
            value = value,
            onValueChange = {},
            label = "Title",
            readOnly = true,
            trailingIcon = Icons.Filled.ArrowDropDown,
            modifier = Modifier.fillMaxWidth(),
        )
        // Transparent tap target over the field opens the menu (the field itself never focuses).
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TITLES.forEach { title ->
                DropdownMenuItem(text = { Text(title) }, onClick = { onSelect(title); expanded = false })
            }
        }
    }
}

@Composable
private fun CenteredLoading(label: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(RhSpacing.md))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PriceErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = RhSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(RhSpacing.md))
        RhButton(text = "Retry", onClick = onRetry)
    }
}

@Composable
private fun ProcessingOverlay(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            // Swallow taps so the screen can't be dismissed mid-booking (which would cancel the call).
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(RhSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(RhSpacing.md))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PriceChangeDialog(change: PriceChange, onConfirm: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDecline) { Text("Go back") } },
        title = { Text("Price updated") },
        text = {
            Text(
                "The price for this room changed from ${formatMoney(change.oldTotal, change.currency)} to " +
                    "${formatMoney(change.newTotal, change.currency)}. Continue with the new price?",
            )
        },
    )
}

private fun cardSummary(card: CardForm): String {
    val digits = card.number.filter { it.isDigit() }
    if (digits.isEmpty()) return "—"
    val last4 = digits.takeLast(4)
    val expiry = if (card.expiryMonth.isNotBlank() && card.expiryYear.length == 4) {
        " • ${card.expiryMonth.padStart(2, '0')}/${card.expiryYear.takeLast(2)}"
    } else {
        ""
    }
    return "Visa •••• $last4$expiry"
}
