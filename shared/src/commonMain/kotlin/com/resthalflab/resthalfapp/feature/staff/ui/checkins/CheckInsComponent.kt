package com.resthalflab.resthalfapp.feature.staff.ui.checkins

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.staff.domain.GetCheckInsUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.model.CheckIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckInTab(val label: String) {
    Completed("Completed"),
    Active("Active"),
    ;
    companion object {
        val labels = entries.map { it.label }
    }
}

interface CheckInsComponent {
    val state: StateFlow<UiState>
    fun onTabSelected(index: Int)
    fun onRefresh()

    data class UiState(
        val selectedTabIndex: Int = CheckInTab.Active.ordinal,
        val all: List<CheckIn> = emptyList(),
        val loading: Boolean = false,
        val refreshing: Boolean = false,
        val error: String? = null,
    ) {
        val visible: List<CheckIn> get() = when (CheckInTab.entries[selectedTabIndex]) {
            CheckInTab.Active -> all.filter { it.active }
            CheckInTab.Completed -> all.filter { !it.active }
        }
    }
}

class DefaultCheckInsComponent(
    componentContext: ComponentContext,
    private val getCheckIns: GetCheckInsUseCase,
) : CheckInsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(CheckInsComponent.UiState(loading = true))
    override val state: StateFlow<CheckInsComponent.UiState> = _state.asStateFlow()

    init {
        load(initial = true)
    }

    override fun onRefresh() = load(initial = false)

    override fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTabIndex = index) }
    }

    private fun load(initial: Boolean) {
        scope.launch {
            _state.update { it.copy(loading = initial, refreshing = !initial, error = null) }
            when (val result = getCheckIns()) {
                is AppResult.Success ->
                    _state.update { it.copy(loading = false, refreshing = false, all = result.value) }
                is AppResult.Failure ->
                    _state.update { it.copy(loading = false, refreshing = false, error = result.error.message) }
            }
        }
    }
}
