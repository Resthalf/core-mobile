package com.resthalflab.resthalfapp.feature.staff.ui.rooms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.staff.domain.GetRoomsUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface RoomsComponent {
    val state: StateFlow<UiState>
    fun onRefresh()

    data class UiState(
        val rooms: List<RoomStatus> = emptyList(),
        val loading: Boolean = false,
        val refreshing: Boolean = false,
        val error: String? = null,
    )
}

class DefaultRoomsComponent(
    componentContext: ComponentContext,
    private val getRooms: GetRoomsUseCase,
) : RoomsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(RoomsComponent.UiState(loading = true))
    override val state: StateFlow<RoomsComponent.UiState> = _state.asStateFlow()

    init {
        load(initial = true)
    }

    override fun onRefresh() = load(initial = false)

    private fun load(initial: Boolean) {
        scope.launch {
            _state.update { it.copy(loading = initial, refreshing = !initial, error = null) }
            when (val result = getRooms()) {
                is AppResult.Success ->
                    _state.update { it.copy(loading = false, refreshing = false, rooms = result.value) }
                is AppResult.Failure ->
                    _state.update { it.copy(loading = false, refreshing = false, error = result.error.message) }
            }
        }
    }
}
