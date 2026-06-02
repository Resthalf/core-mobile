package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.coroutines.flow.StateFlow

interface BookingConfirmationComponent {
    val state: StateFlow<State>
    fun onBackClicked()
    fun onRetry()
    fun onViewDetails()
    fun onProceedToPayment()

    sealed interface State {
        data object Loading : State
        data class Error(val message: String) : State
        data class Content(val confirmation: BookingConfirmation) : State
    }
}
