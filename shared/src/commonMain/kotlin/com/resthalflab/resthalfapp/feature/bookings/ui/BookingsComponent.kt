package com.resthalflab.resthalfapp.feature.bookings.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnResume
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingsUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BookingTab(val label: String) {
    Upcoming("Upcoming"),
    Past("Past"),
    ;
    companion object {
        val labels = entries.map { it.label }

        // Trips still ahead (or in progress); everything else is Past.
        val UPCOMING = setOf(BookingStatus.Pending, BookingStatus.Confirmed, BookingStatus.Active)
    }
}

interface BookingsComponent {
    val state: StateFlow<UiState>
    fun onTabSelected(index: Int)
    fun onBookingClicked(id: String)
    fun onRefresh()

    data class UiState(
        val selectedTabIndex: Int = 0,
        val allBookings: List<Booking> = emptyList(),
        val loading: Boolean = false,
        val refreshing: Boolean = false,
        val error: String? = null,
    ) {
        val visibleBookings: List<Booking> get() = when (BookingTab.entries[selectedTabIndex]) {
            BookingTab.Upcoming -> allBookings.filter { it.status in BookingTab.UPCOMING }
            BookingTab.Past -> allBookings.filter { it.status !in BookingTab.UPCOMING }
        }
    }
}

class DefaultBookingsComponent(
    componentContext: ComponentContext,
    private val getBookings: GetBookingsUseCase,
    private val onOpenBookingDetail: (String) -> Unit = {},
) : BookingsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(BookingsComponent.UiState(loading = true))
    override val state: StateFlow<BookingsComponent.UiState> = _state.asStateFlow()

    private var loadedOnce = false

    init {
        // Reload whenever the list is shown again (tab switch, or returning from detail after a
        // cancel/reschedule/vacate), so status changes are reflected without a manual pull-to-refresh.
        lifecycle.doOnResume {
            load(initial = !loadedOnce)
            loadedOnce = true
        }
    }

    override fun onRefresh() = load(initial = false)

    private fun load(initial: Boolean) {
        scope.launch {
            _state.update { it.copy(loading = initial, refreshing = !initial, error = null) }
            when (val result = getBookings()) {
                is AppResult.Success ->
                    _state.update { it.copy(loading = false, refreshing = false, allBookings = result.value) }
                is AppResult.Failure ->
                    _state.update { it.copy(loading = false, refreshing = false, error = result.error.message) }
            }
        }
    }

    override fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTabIndex = index) }
    }

    override fun onBookingClicked(id: String) = onOpenBookingDetail(id)
}
