package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.coroutines.flow.StateFlow

interface PaymentComponent {
    val paymentUrl: String
    val state: StateFlow<UiState>
    fun onSimulatePayment()
    fun onBackClicked()

    data class UiState(
        val submitting: Boolean = false,
        val error: String? = null,
    )
}
