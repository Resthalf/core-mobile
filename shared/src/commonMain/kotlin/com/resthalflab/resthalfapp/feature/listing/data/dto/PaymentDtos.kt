package com.resthalflab.resthalfapp.feature.listing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors a Midtrans notification body — used only by the sandbox simulation. */
@Serializable
data class PaymentWebhookRequest(
    @SerialName("order_id") val orderId: String,
    @SerialName("status_code") val statusCode: String,
    @SerialName("gross_amount") val grossAmount: String,
    @SerialName("signature_key") val signatureKey: String,
    @SerialName("transaction_status") val transactionStatus: String,
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("payment_type") val paymentType: String,
    @SerialName("fraud_status") val fraudStatus: String,
)

@Serializable
data class PaymentWebhookResponse(
    val status: String,
    val action: String? = null,
)
