package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.api.LocalBookingStore
import com.resthalflab.resthalfapp.feature.listing.data.dto.PaymentWebhookResponse

/**
 * Marks a booking paid on-device (RestHalf backend retired). Replaces the old sandbox webhook +
 * on-device signature; the real payment flow (Midtrans Snap redirect + server-to-server webhook)
 * lands when a payment backend is wired.
 */
class SimulatePaymentUseCase(
    private val store: LocalBookingStore,
) {
    suspend operator fun invoke(bookingId: String): AppResult<PaymentWebhookResponse> = try {
        if (store.get(bookingId) == null) {
            AppResult.Failure(AppError.Unknown("Booking not found"))
        } else {
            store.markPaid(bookingId)
            AppResult.Success(PaymentWebhookResponse(status = "ok"))
        }
    } catch (t: Throwable) {
        AppResult.Failure(AppError.Unknown("Payment failed", t))
    }
}
