package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.coroutines.flow.StateFlow

interface ListingDetailComponent {
    val state: StateFlow<State>
    fun onBackClicked()
    fun onRetry()

    sealed interface State {
        data object Loading : State
        data class Error(val message: String) : State
        data class Content(val detail: ListingDetail) : State
    }
}
