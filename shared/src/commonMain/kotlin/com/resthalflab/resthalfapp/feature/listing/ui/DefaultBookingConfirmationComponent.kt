package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmation
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent.State
import com.resthalflab.resthalfapp.feature.listing.domain.SlotDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class DefaultBookingConfirmationComponent(
    componentContext: ComponentContext,
    private val args: BookingConfirmationArgs,
    private val onBack: () -> Unit,
) : BookingConfirmationComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow<State>(State.Content(buildConfirmation()))
    override val state: StateFlow<State> = _state.asStateFlow()

    private fun buildConfirmation(): BookingConfirmation {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val month = today.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return BookingConfirmation(
            bookingId = args.orderId,
            hotelName = args.hotelName,
            roomType = "Room ${args.roomNumber}",
            dateLabel = "${today.dayOfMonth} $month ${today.year}",
            stayWindow = "${SlotDisplay.title(args.slotType)} · ${SlotDisplay.windowShort(args.slotType)}",
            guestsLabel = "1 Adult",
            totalPaid = formatMoney(args.amount, args.currency),
        )
    }

    override fun onRetry() { /* nothing to retry — built from args */ }
    override fun onBackClicked() = onBack()
    override fun onViewDetails() = onBack()
    override fun onProceedToPayment() { /* TODO Phase 3: payment flow */ }
}
