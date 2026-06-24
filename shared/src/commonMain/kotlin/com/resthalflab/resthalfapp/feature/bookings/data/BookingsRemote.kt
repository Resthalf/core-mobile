package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.feature.bookings.data.dto.MyBookingsResponse
import com.resthalflab.resthalfapp.feature.bookings.data.dto.VacateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post

/** Uses the default (authenticated) client — /my/bookings is scoped to the signed-in guest. */
class BookingsRemote(
    private val client: HttpClient,
) {
    suspend fun getMyBookings(): MyBookingsResponse =
        client.get("my/bookings").body()

    /** Guest releases their room early. Path is the active delegation id. */
    suspend fun vacate(delegationId: String): VacateResponse =
        client.post("vacate/$delegationId").body()
}
