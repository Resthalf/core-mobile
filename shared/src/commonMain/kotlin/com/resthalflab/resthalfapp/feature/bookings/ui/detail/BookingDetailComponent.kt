package com.resthalflab.resthalfapp.feature.bookings.ui.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.time.Clock

interface BookingDetailComponent {
    val state: StateFlow<State>
    fun onBackClicked()
    fun onRetry()
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
            /** 0.0 = just checked in, 1.0 = checkout time reached. */
            val checkoutProgress: Float,
        ) : State
    }
}

class DefaultBookingDetailComponent(
    componentContext: ComponentContext,
    private val getBookingById: GetBookingByIdUseCase,
    private val bookingId: String,
    private val onBack: () -> Unit,
) : BookingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<BookingDetailComponent.State>(BookingDetailComponent.State.Loading)
    override val state: StateFlow<BookingDetailComponent.State> = _state.asStateFlow()

    init { load() }

    override fun onRetry() = load()
    override fun onBackClicked() = onBack()
    override fun onHotelInfoClicked() {}
    override fun onCallHotelClicked() {}
    override fun onWhatsAppClicked() {}
    override fun onGetDirectionClicked() {}

    private fun load() {
        scope.launch {
            _state.value = BookingDetailComponent.State.Loading
            when (val result = getBookingById(bookingId)) {
                is AppResult.Success -> startTicker(result.value)
                is AppResult.Failure -> _state.value = BookingDetailComponent.State.Error(result.error.message)
            }
        }
    }

    private fun startTicker(booking: Booking) {
        scope.launch {
            while (isActive) {
                _state.value = BookingDetailComponent.State.Content(
                    booking = booking,
                    hoursLeft = computeHours(booking),
                    minutesLeft = computeMinutes(booking),
                    secondsLeft = computeSeconds(booking),
                    checkoutProgress = computeProgress(booking),
                )
                delay(1_000)
            }
        }
    }

    // ── Countdown helpers ─────────────────────────────────────────────────────
    // For Active bookings: counts down to 12:00 PM today.
    // For all other statuses: seconds/minutes/hours are pinned to 0 (stay is over).

    private fun secondsUntilCheckout(booking: Booking): Long {
        if (booking.status != BookingStatus.Active) return 0L
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val local = now.toLocalDateTime(tz)
        // Target: 12:00:00 today. If already past noon pin to 0.
        val todayNoon = LocalDateTime(
            year = local.year,
            monthNumber = local.monthNumber,
            dayOfMonth = local.dayOfMonth,
            hour = 12,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
        val targetInstant = todayNoon.toInstant(tz)
        return maxOf(0L, (targetInstant - now).inWholeSeconds)
    }

    private fun computeHours(booking: Booking): Int = (secondsUntilCheckout(booking) / 3600).toInt()
    private fun computeMinutes(booking: Booking): Int = ((secondsUntilCheckout(booking) % 3600) / 60).toInt()
    private fun computeSeconds(booking: Booking): Int = (secondsUntilCheckout(booking) % 60).toInt()

    private fun computeProgress(booking: Booking): Float {
        if (booking.status != BookingStatus.Active) return 1f
        val totalSeconds = 12 * 3600f
        val remainingSeconds = secondsUntilCheckout(booking).toFloat()
        return 1f - (remainingSeconds / totalSeconds).coerceIn(0f, 1f)
    }
}
