package com.resthalflab.resthalfapp.feature.bookings.ui.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnResume
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingByIdUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.VacateBookingUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface BookingDetailComponent {
    val state: StateFlow<State>
    /** Vacate flow state, kept separate from [state] so the per-second ticker doesn't clobber it. */
    val vacate: StateFlow<VacateState>
    fun onBackClicked()
    fun onRetry()
    fun onProceedToPayment()
    fun onConfirmVacate()
    fun onDismissVacateResult()
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

    data class VacateState(
        val submitting: Boolean = false,
        val successMessage: String? = null,
        val error: String? = null,
    )
}

class DefaultBookingDetailComponent(
    componentContext: ComponentContext,
    private val getBookingById: GetBookingByIdUseCase,
    private val vacateBooking: VacateBookingUseCase,
    private val bookingId: String,
    private val onBack: () -> Unit,
    private val onPay: (Booking) -> Unit = {},
) : BookingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<BookingDetailComponent.State>(BookingDetailComponent.State.Loading)
    override val state: StateFlow<BookingDetailComponent.State> = _state.asStateFlow()

    private val _vacate = MutableStateFlow(BookingDetailComponent.VacateState())
    override val vacate: StateFlow<BookingDetailComponent.VacateState> = _vacate.asStateFlow()

    private var loadedBooking: Booking? = null
    private var loadJob: Job? = null

    // Reload on every resume (not just first creation): when we pop back from Payment, the booking
    // may now be flagged Internal Error, so the stale Pending UI must refresh.
    init { lifecycle.doOnResume { load() } }

    override fun onRetry() = load()
    override fun onBackClicked() = onBack()
    override fun onProceedToPayment() { loadedBooking?.let(onPay) }
    override fun onHotelInfoClicked() {}
    override fun onCallHotelClicked() {}
    override fun onWhatsAppClicked() {}
    override fun onGetDirectionClicked() {}

    override fun onConfirmVacate() {
        val delegationId = loadedBooking?.delegationId ?: return
        if (_vacate.value.submitting) return
        scope.launch {
            _vacate.value = BookingDetailComponent.VacateState(submitting = true)
            _vacate.value = when (val result = vacateBooking(delegationId)) {
                is AppResult.Success -> BookingDetailComponent.VacateState(successMessage = result.value.message)
                is AppResult.Failure -> BookingDetailComponent.VacateState(error = result.error.message)
            }
        }
    }

    override fun onDismissVacateResult() {
        val wasSuccess = _vacate.value.successMessage != null
        _vacate.value = BookingDetailComponent.VacateState()
        if (wasSuccess) load() // refresh so the stay no longer reads as active
    }

    private fun load() {
        loadJob?.cancel() // also stops any running ticker, which lives inside this job
        loadJob = scope.launch {
            _state.value = BookingDetailComponent.State.Loading
            when (val result = getBookingById(bookingId)) {
                is AppResult.Success -> {
                    loadedBooking = result.value
                    runTicker(result.value)
                }
                is AppResult.Failure -> _state.value = BookingDetailComponent.State.Error(result.error.message)
            }
        }
    }

    // Counts down to the booking's endTime (delegation window for active stays). For non-active
    // bookings the window is already in the past, so the timer reads 0 / progress 1 and stops.
    // Runs inside [loadJob] so a reload cancels it cleanly.
    private suspend fun runTicker(booking: Booking) {
        val ticking = booking.status == BookingStatus.Active
        while (true) {
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
