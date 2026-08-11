package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.coroutines.flow.StateFlow

interface PaymentComponent {
    val paymentUrl: String
    val state: StateFlow<UiState>
    fun onSimulatePayment()
    fun onBackClicked()
    fun onDismissInternalError()

    data class UiState(
        val submitting: Boolean = false,
        val error: String? = null,
        /** Set when the backend rejects the payment irrecoverably (e.g. slot expired). Drives a blocking dialog. */
        val internalError: Boolean = false,
    )
}
