package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.domain.formatMoney
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase
import com.resthalflab.resthalfapp.feature.listing.domain.SlotDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultListingDetailComponent(
    componentContext: ComponentContext,
    private val selection: RoomSelection,
    private val createBooking: CreateBookingUseCase,
    private val onBack: () -> Unit,
    private val onBooked: (BookingConfirmationArgs) -> Unit,
) : ListingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(
        ListingDetailComponent.UiState(
            hotelName = selection.hotelName,
            city = selection.city,
            roomNumber = selection.roomNumber,
            roomId = selection.roomId,
            stayTitle = SlotDisplay.title(selection.slotType),
            stayWindowLine = SlotDisplay.windowLine(selection.slotType),
            checkInOutLine = SlotDisplay.checkInOutLine(selection.slotType),
            photoUrls = (1..6).map { "https://picsum.photos/seed/${selection.hotelId}_$it/900/600" },
            priceLabel = formatMoney(selection.price, selection.currency),
            bookButtonText = "Book ${SlotDisplay.title(selection.slotType)}",
        )
    )
    override val state: StateFlow<ListingDetailComponent.UiState> = _state.asStateFlow()

    override fun onBackClicked() = onBack()

    override fun onBookClicked() {
        if (_state.value.submitting) return
        scope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            when (
                val result = createBooking(
                    selection.roomId,
                    selection.slotType,
                    selection.startTime,
                    selection.endTime,
                )
            ) {
                is AppResult.Success -> onBooked(
                    BookingConfirmationArgs(
                        bookingId = result.value.bookingId,
                        orderId = result.value.orderId,
                        hotelName = selection.hotelName,
                        roomNumber = selection.roomNumber,
                        slotType = selection.slotType,
                        amount = result.value.amount,
                        currency = selection.currency,
                    )
                )
                is AppResult.Failure -> _state.update { it.copy(submitting = false, error = result.error.message) }
            }
        }
    }
}
