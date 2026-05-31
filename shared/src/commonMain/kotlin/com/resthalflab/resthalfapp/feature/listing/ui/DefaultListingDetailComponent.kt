package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent.State
import com.resthalflab.resthalfapp.feature.listing.domain.GetListingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultListingDetailComponent(
    componentContext: ComponentContext,
    private val getListing: GetListingUseCase,
    private val listingId: String,
    private val onBack: () -> Unit,
) : ListingDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow<State>(State.Loading)
    override val state: StateFlow<State> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        scope.launch {
            _state.value = State.Loading
            when (val result = getListing(listingId)) {
                is AppResult.Success -> _state.value = State.Content(result.value)
                is AppResult.Failure -> _state.value = State.Error(result.error.message)
            }
        }
    }

    override fun onRetry() = load()
    override fun onBackClicked() = onBack()
}
