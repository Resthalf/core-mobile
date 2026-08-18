package com.resthalflab.resthalfapp.feature.staff.ui.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.ui.home.DefaultHomeComponent
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeComponent
import com.resthalflab.resthalfapp.feature.staff.domain.ConfirmVacateUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.GetRoomsUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface StaffHomeComponent {
    /** Drives the reusable search planner card. */
    val home: HomeComponent
    val state: StateFlow<UiState>

    fun onRefresh()
    fun onViewDetails(roomId: String)
    fun onConfirmVacate(roomId: String, delegationId: String, notes: String)

    data class UiState(
        val rooms: List<RoomStatus> = emptyList(),
        val loading: Boolean = false,
        val refreshing: Boolean = false,
        val error: String? = null,
        /** Room id whose vacate request is in flight, for per-row button state. */
        val vacatingRoomId: String? = null,
    )
}

class DefaultStaffHomeComponent(
    componentContext: ComponentContext,
    locationSearch: LocationSearchApi,
    onSearch: (SearchArgs) -> Unit,
    private val getRooms: GetRoomsUseCase,
    private val confirmVacate: ConfirmVacateUseCase,
    private val onOpenRooms: (String) -> Unit,
) : StaffHomeComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)

    override val home: HomeComponent = DefaultHomeComponent(
        componentContext = componentContext,
        locationSearch = locationSearch,
        onSearch = onSearch,
    )

    private val _state = MutableStateFlow(StaffHomeComponent.UiState(loading = true))
    override val state: StateFlow<StaffHomeComponent.UiState> = _state.asStateFlow()

    init {
        load(initial = true)
    }

    override fun onRefresh() = load(initial = false)

    override fun onViewDetails(roomId: String) = onOpenRooms(roomId)

    override fun onConfirmVacate(roomId: String, delegationId: String, notes: String) {
        scope.launch {
            _state.update { it.copy(vacatingRoomId = roomId, error = null) }
            when (val result = confirmVacate(roomId, delegationId, notes)) {
                is AppResult.Success -> {
                    _state.update { it.copy(vacatingRoomId = null) }
                    load(initial = false) // refresh so the vacated room drops off the list
                }
                is AppResult.Failure ->
                    _state.update { it.copy(vacatingRoomId = null, error = result.error.message) }
            }
        }
    }

    private fun load(initial: Boolean) {
        scope.launch {
            _state.update { it.copy(loading = initial, refreshing = !initial, error = null) }
            when (val result = getRooms()) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, rooms = result.value.toNextVacated())
                    }
                is AppResult.Failure ->
                    _state.update { it.copy(loading = false, refreshing = false, error = result.error.message) }
            }
        }
    }

    // Occupied rooms closest to their end time — the ones a staffer is about to turn over.
    private fun List<RoomStatus>.toNextVacated(): List<RoomStatus> =
        filter { it.isOccupied }.sortedBy { it.timeLeftSeconds }.take(MAX_NEXT_VACATED)

    private companion object {
        const val MAX_NEXT_VACATED = 5
    }
}
