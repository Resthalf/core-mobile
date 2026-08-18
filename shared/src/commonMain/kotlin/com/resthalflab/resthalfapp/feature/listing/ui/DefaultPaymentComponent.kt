package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.storage.FailedBookingStore
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.PaymentComponent
import com.resthalflab.resthalfapp.feature.listing.domain.PaymentConfig
import com.resthalflab.resthalfapp.feature.listing.domain.SimulatePaymentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultPaymentComponent(
    componentContext: ComponentContext,
    private val args: BookingConfirmationArgs,
    private val simulatePayment: SimulatePaymentUseCase,
    private val failedBookingStore: FailedBookingStore,
    private val onPaid: () -> Unit,
    private val onBack: () -> Unit,
) : PaymentComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    override val paymentUrl: String = PaymentConfig.SANDBOX_PAYMENT_URL

    private val _state = MutableStateFlow(PaymentComponent.UiState())
    override val state: StateFlow<PaymentComponent.UiState> = _state.asStateFlow()

    override fun onBackClicked() = onBack()

    // Dismissing the irrecoverable-error dialog leaves the payment screen; the booking is now flagged
    // as Internal Error and shows disabled in the bookings list.
    override fun onDismissInternalError() {
        _state.update { it.copy(internalError = false) }
        onBack()
    }

    override fun onSimulatePayment() {
        if (_state.value.submitting) return
        scope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            when (val result = simulatePayment(args.bookingId)) {
                is AppResult.Success ->
                    if (result.value.status.equals("ok", ignoreCase = true)) {
                        onPaid()
                    } else {
                        _state.update { it.copy(submitting = false, error = "Payment not confirmed") }
                    }
                is AppResult.Failure -> handleFailure(result.error)
            }
        }
    }

    private fun handleFailure(error: AppError) {
        // A 5xx from the webhook means the backend can't settle this booking (slot expired / no slots).
        // Flag it so the bookings list disables it, and raise a blocking dialog instead of an inline error.
        val isServerError = error is AppError.Network.Server && error.statusCode >= 500
        if (isServerError) {
            failedBookingStore.markFailed(args.bookingId)
            _state.update { it.copy(submitting = false, internalError = true) }
        } else {
            _state.update { it.copy(submitting = false, error = error.message) }
        }
    }
}
