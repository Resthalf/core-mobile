package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.domain.sha512Hex
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.listing.data.PaymentRemote
import com.resthalflab.resthalfapp.feature.listing.data.dto.PaymentWebhookRequest
import com.resthalflab.resthalfapp.feature.listing.data.dto.PaymentWebhookResponse
import kotlin.time.Clock

/**
 * Fakes a successful Midtrans settlement by POSTing the webhook ourselves. Sandbox only —
 * see [PaymentConfig]. Mirrors the backend's signature: sha512(orderId + statusCode + gross + serverKey).
 */
class SimulatePaymentUseCase(
    private val remote: PaymentRemote,
) {
    suspend operator fun invoke(orderId: String, amount: Int): AppResult<PaymentWebhookResponse> = safeApiCall {
        val statusCode = "200"
        val gross = "$amount.00"
        val signature = sha512Hex("$orderId$statusCode$gross${PaymentConfig.SANDBOX_SERVER_KEY}")
        remote.simulateWebhook(
            PaymentWebhookRequest(
                orderId = orderId,
                statusCode = statusCode,
                grossAmount = gross,
                signatureKey = signature,
                transactionStatus = "settlement",
                transactionId = "SIMULATED-${Clock.System.now().toEpochMilliseconds()}",
                paymentType = "qris",
                fraudStatus = "accept",
            )
        )
    }
}
