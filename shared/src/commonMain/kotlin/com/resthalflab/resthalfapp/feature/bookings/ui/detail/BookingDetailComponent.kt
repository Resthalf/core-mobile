package com.resthalflab.resthalfapp.feature.bookings.ui.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingByIdUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface BookingDetailComponent {
    val state: StateFlow<State>
    fun onBackClicked()
    fun onRetry()
    fun onProceedToPayment()
    fun onHotelInfoClicked()
    fun onCallHotelClicked()
    fun onWhatsAppClicked()
    fun onGetDirectionClicked()

    sealed interface State {
        data object Loading : State
        data class Error(val message: String) : State
        data class Content(
            val booking: Booking,
            val hoursLeft: Int,
            val minutesLeft: Int,
            val secondsLeft: Int,
            /** 0.0 = just checked in, 1.0 = checkout reached. */
            val checkoutProgress: Float,
        ) : State
    }
}

class DefaultBookingDetailComponent(
    componentContext: ComponentContext,
    private val getBookingById: GetBookingByIdUseCase,
    private val bookingId: String,
    private val onBack: () -> Unit,
    private val onPay: (Booking) -> Unit = {},
) : BookingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<BookingDetailComponent.State>(BookingDetailComponent.State.Loading)
    override val state: StateFlow<BookingDetailComponent.State> = _state.asStateFlow()

    private var loadedBooking: Booking? = null

    init { load() }

    override fun onRetry() = load()
    override fun onBackClicked() = onBack()
    override fun onProceedToPayment() { loadedBooking?.let(onPay) }
    override fun onHotelInfoClicked() {}
    override fun onCallHotelClicked() {}
    override fun onWhatsAppClicked() {}
    override fun onGetDirectionClicked() {}

    private fun load() {
        scope.launch {
            _state.value = BookingDetailComponent.State.Loading
            when (val result = getBookingById(bookingId)) {
                is AppResult.Success -> {
                    loadedBooking = result.value
                    startTicker(result.value)
                }
                is AppResult.Failure -> _state.value = BookingDetailComponent.State.Error(result.error.message)
            }
        }
    }

    // Counts down to the booking's endTime (delegation window for active stays). For non-active
    // bookings the window is already in the past, so the timer naturally reads 0 / progress 1.
    private fun startTicker(booking: Booking) {
        val ticking = booking.status == BookingStatus.Active
        scope.launch {
            while (isActive) {
                val (h, m, s) = BookingTime.parts(booking.endTime)
                _state.value = BookingDetailComponent.State.Content(
                    booking = booking,
                    hoursLeft = h,
                    minutesLeft = m,
                    secondsLeft = s,
                    checkoutProgress = BookingTime.progress(booking.startTime, booking.endTime),
                )
                if (!ticking) break
                delay(1_000)
            }
        }
    }
}
