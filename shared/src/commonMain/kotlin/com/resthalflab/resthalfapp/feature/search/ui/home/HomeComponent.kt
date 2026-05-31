package com.resthalflab.resthalfapp.feature.search.ui.home

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface HomeComponent {
    val state: StateFlow<UiState>
    fun onDestinationChanged(value: String)
    fun onSearchClicked()

    data class UiState(
        val destination: String = "Jakarta",
        // Static placeholders until the date/stay-window pickers land (Phase 3).
        val dateLabel: String = "Fri, 24 May 2024",
        val stayWindowTitle: String = "Stay window",
        val stayWindowSubtitle: String = "12:00 AM – 12:00 PM (12 hours)",
    )
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onSearch: (destination: String) -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(HomeComponent.UiState())
    override val state: StateFlow<HomeComponent.UiState> = _state.asStateFlow()

    override fun onDestinationChanged(value: String) {
        _state.update { it.copy(destination = value) }
    }

    override fun onSearchClicked() {
        onSearch(_state.value.destination)
    }
}
