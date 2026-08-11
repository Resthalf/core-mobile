package com.resthalflab.resthalfapp.feature.listing.data

import com.resthalflab.resthalfapp.feature.listing.data.dto.PaymentWebhookRequest
import com.resthalflab.resthalfapp.feature.listing.data.dto.PaymentWebhookResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PaymentRemote(
    private val client: HttpClient,
) {
    suspend fun simulateWebhook(request: PaymentWebhookRequest): PaymentWebhookResponse =
        client.post("payment/webhook") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
