package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.feature.bookings.data.dto.MyBookingsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Uses the default (authenticated) client — /my/bookings is scoped to the signed-in guest. */
class BookingsRemote(
    private val client: HttpClient,
) {
    suspend fun getMyBookings(): MyBookingsResponse =
        client.get("my/bookings").body()
}
