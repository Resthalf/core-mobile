package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmation
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent.State
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail
import com.resthalflab.resthalfapp.feature.listing.domain.GetListingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class DefaultBookingConfirmationComponent(
    componentContext: ComponentContext,
    private val getListing: GetListingUseCase,
    private val listingId: String,
    private val onBack: () -> Unit,
) : BookingConfirmationComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<State>(State.Loading)
    override val state: StateFlow<State> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        scope.launch {
            _state.value = State.Loading
            when (val result = getListing(listingId)) {
                is AppResult.Success -> _state.value = State.Content(result.value.toConfirmation())
                is AppResult.Failure -> _state.value = State.Error(result.error.message)
            }
        }
    }

    // Phase 3: a real booking POST returns the confirmation; for now derive it from the listing.
    private fun ListingDetail.toConfirmation(): BookingConfirmation {
        val suffix = abs(id.hashCode() % 10_000).toString().padStart(4, '0')
        return BookingConfirmation(
            bookingId = "RH240524$suffix",
            hotelName = name,
            roomType = roomType,
            dateLabel = "Fri, 24 May 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            guestsLabel = "$guests Adults",
            totalPaid = formatMoney(totalPrice, currency),
        )
    }

    override fun onRetry() = load()
    override fun onBackClicked() = onBack()
    override fun onViewDetails() = onBack()
    override fun onProceedToPayment() { /* TODO Phase 3: payment flow */ }
}
