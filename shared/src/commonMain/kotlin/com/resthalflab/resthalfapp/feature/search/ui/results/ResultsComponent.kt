package com.resthalflab.resthalfapp.feature.search.ui.results

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.domain.SearchListingsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import com.resthalflab.resthalfapp.feature.search.domain.model.SearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ResultsComponent {
    val state: StateFlow<UiState>
    fun onListingClicked(id: String)
    fun onBackClicked()
    fun onRetry()

    data class UiState(
        val destination: String,
        // Static placeholders mirroring the home form until the pickers land (Phase 3).
        val dateLabel: String = "Fri, 24 May 2024",
        val stayWindow: String = "12AM – 12PM",
        val loading: Boolean = false,
        val results: List<Listing> = emptyList(),
        val error: String? = null,
    )
}

class DefaultResultsComponent(
    componentContext: ComponentContext,
    private val searchListings: SearchListingsUseCase,
    private val destination: String,
    private val onListingSelected: (String) -> Unit,
    private val onBack: () -> Unit,
) : ResultsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(ResultsComponent.UiState(destination = destination))
    override val state: StateFlow<ResultsComponent.UiState> = _state.asStateFlow()

    init {
        search()
    }

    private fun search() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = searchListings(SearchQuery(destination = destination))) {
                is AppResult.Success -> _state.update { it.copy(loading = false, results = result.value) }
                is AppResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
            }
        }
    }

    override fun onListingClicked(id: String) = onListingSelected(id)
    override fun onBackClicked() = onBack()
    override fun onRetry() = search()
}
