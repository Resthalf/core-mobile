package com.resthalflab.resthalfapp.feature.bookings.ui.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnResume
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.CancelBookingUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingByIdUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.GetCancelPreviewUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.VacateBookingUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import com.resthalflab.resthalfapp.feature.bookings.domain.model.CancelPreview
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
    /** Cancellation flow state (preview → confirm → result), likewise kept off the ticking [state]. */
    val cancel: StateFlow<CancelState>
    fun onBackClicked()
    fun onRetry()
    fun onProceedToPayment()
    fun onConfirmVacate()
    fun onDismissVacateResult()
    fun onCancelReservation()
    fun onConfirmCancel()
    fun onDismissCancel()
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
            /** True for an upcoming Confirmed booking still outside the cancellation deadline. */
            val canCancel: Boolean,
            /** True while the stay hasn't begun — the countdown targets startTime ("Starts in"). */
            val countingToStart: Boolean,
        ) : State
    }

    data class VacateState(
        val submitting: Boolean = false,
        val successMessage: String? = null,
        val error: String? = null,
    )

    data class CancelState(
        val loadingPreview: Boolean = false,
        /** Non-null once the eligibility check returns — drives the confirm/info dialog. */
        val preview: CancelPreview? = null,
        val submitting: Boolean = false,
        val done: Boolean = false,
        val error: String? = null,
    ) {
        val busy: Boolean get() = loadingPreview || submitting
    }
}

class DefaultBookingDetailComponent(
    componentContext: ComponentContext,
    private val getBookingById: GetBookingByIdUseCase,
    private val vacateBooking: VacateBookingUseCase,
    private val getCancelPreview: GetCancelPreviewUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val bookingId: String,
    private val onBack: () -> Unit,
    private val onPay: (Booking) -> Unit = {},
) : BookingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<BookingDetailComponent.State>(BookingDetailComponent.State.Loading)
    override val state: StateFlow<BookingDetailComponent.State> = _state.asStateFlow()

    private val _vacate = MutableStateFlow(BookingDetailComponent.VacateState())
    override val vacate: StateFlow<BookingDetailComponent.VacateState> = _vacate.asStateFlow()

    private val _cancel = MutableStateFlow(BookingDetailComponent.CancelState())
    override val cancel: StateFlow<BookingDetailComponent.CancelState> = _cancel.asStateFlow()

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

    // Step 1: check eligibility + refund. The dialog the screen raises off [preview] confirms it.
    override fun onCancelReservation() {
        if (_cancel.value.busy) return
        scope.launch {
            _cancel.value = BookingDetailComponent.CancelState(loadingPreview = true)
            _cancel.value = when (val result = getCancelPreview(bookingId)) {
                is AppResult.Success -> BookingDetailComponent.CancelState(preview = result.value)
                is AppResult.Failure -> BookingDetailComponent.CancelState(error = result.error.message)
            }
        }
    }

    // Step 2: actually cancel (only reachable when the preview allowed it).
    override fun onConfirmCancel() {
        if (_cancel.value.submitting) return
        scope.launch {
            _cancel.value = _cancel.value.copy(submitting = true, error = null)
            _cancel.value = when (val result = cancelBooking(bookingId, CANCEL_REASON)) {
                is AppResult.Success -> BookingDetailComponent.CancelState(done = true)
                is AppResult.Failure -> BookingDetailComponent.CancelState(error = result.error.message)
            }
        }
    }

    override fun onDismissCancel() {
        val wasDone = _cancel.value.done
        _cancel.value = BookingDetailComponent.CancelState()
        if (wasDone) load() // refresh so the booking now reads as Cancelled
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
        // Confirmed bookings also tick: before start they count down to startTime ("Starts in"),
        // then flip to the checkout countdown once the window opens (still Confirmed until check-in).
        val ticking = booking.status == BookingStatus.Active || booking.status == BookingStatus.Confirmed
        while (true) {
            val beforeStart = !BookingTime.hasStarted(booking.startTime)
            val target = if (beforeStart) booking.startTime else booking.endTime
            val (h, m, s) = BookingTime.parts(target)
            // Cancellable only while upcoming and still more than the free-cancellation window away.
            val canCancel = booking.status == BookingStatus.Confirmed &&
                BookingTime.minutesUntilStart(booking.startTime) > CANCEL_WINDOW_MINUTES
            _state.value = BookingDetailComponent.State.Content(
                booking = booking,
                hoursLeft = h,
                minutesLeft = m,
                secondsLeft = s,
                checkoutProgress = BookingTime.progress(booking.startTime, booking.endTime),
                canCancel = canCancel,
                countingToStart = beforeStart,
            )
            if (!ticking) break
            delay(1_000)
        }
    }

    private companion object {
        const val CANCEL_REASON = "Change of plans"
        const val CANCEL_WINDOW_MINUTES = 120L
    }
}
