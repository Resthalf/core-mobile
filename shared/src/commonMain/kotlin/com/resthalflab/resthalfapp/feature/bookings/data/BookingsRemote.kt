package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.feature.bookings.data.dto.CancelPreviewResponse
import com.resthalflab.resthalfapp.feature.bookings.data.dto.CancelRequest
import com.resthalflab.resthalfapp.feature.bookings.data.dto.CancelResponse
import com.resthalflab.resthalfapp.feature.bookings.data.dto.MyBookingsResponse
import com.resthalflab.resthalfapp.feature.bookings.data.dto.VacateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Uses the default (authenticated) client — /my/bookings is scoped to the signed-in guest. */
class BookingsRemote(
    private val client: HttpClient,
) {
    suspend fun getMyBookings(): MyBookingsResponse =
        client.get("my/bookings").body()

    /** Guest releases their room early. Path is the active delegation id. */
    suspend fun vacate(delegationId: String): VacateResponse =
        client.post("vacate/$delegationId").body()

    /** Cancellation eligibility + refund preview for a not-yet-started booking. */
    suspend fun cancelPreview(bookingId: String): CancelPreviewResponse =
        client.get("bookings/$bookingId/cancel-preview").body()

    suspend fun cancel(bookingId: String, reason: String): CancelResponse =
        client.post("bookings/$bookingId/cancel") {
            contentType(ContentType.Application.Json)
            setBody(CancelRequest(reason))
        }.body()
}
