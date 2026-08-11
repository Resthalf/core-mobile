package com.resthalflab.resthalfapp.feature.staff.data

import com.resthalflab.resthalfapp.feature.staff.data.dto.CheckInDto
import com.resthalflab.resthalfapp.feature.staff.data.dto.ConfirmVacatedRequest
import com.resthalflab.resthalfapp.feature.staff.data.dto.ConfirmVacatedResponse
import com.resthalflab.resthalfapp.feature.staff.data.dto.DashboardRoomDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Staff dashboard endpoints. Uses the default (authenticated) client — all require the Bearer token. */
class DashboardRemote(
    private val client: HttpClient,
) {
    suspend fun getCheckins(): List<CheckInDto> =
        client.get("dashboard/checkins").body()

    suspend fun getRooms(): List<DashboardRoomDto> =
        client.get("dashboard/rooms").body()

    suspend fun confirmVacated(roomId: String, request: ConfirmVacatedRequest): ConfirmVacatedResponse =
        client.post("dashboard/rooms/$roomId/confirm-vacated") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
