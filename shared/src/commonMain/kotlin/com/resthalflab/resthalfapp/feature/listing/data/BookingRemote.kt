package com.resthalflab.resthalfapp.feature.listing.data

import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectRequest
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Uses the default (authenticated) client — booking requires the Bearer token. */
class BookingRemote(
    private val client: HttpClient,
) {
    suspend fun createDirect(request: BookingDirectRequest): BookingDirectResponse =
        client.post("bookings/direct") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
