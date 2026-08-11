package com.resthalflab.resthalfapp.feature.staff.data

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.staff.data.dto.CheckInDto
import com.resthalflab.resthalfapp.feature.staff.data.dto.ConfirmVacatedRequest
import com.resthalflab.resthalfapp.feature.staff.data.dto.DashboardRoomDto
import com.resthalflab.resthalfapp.feature.staff.domain.DashboardRepository
import com.resthalflab.resthalfapp.feature.staff.domain.StaffTime
import com.resthalflab.resthalfapp.feature.staff.domain.model.CheckIn
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus

class DefaultDashboardRepository(
    private val remote: DashboardRemote,
) : DashboardRepository {

    override suspend fun getCheckins(): AppResult<List<CheckIn>> = safeApiCall {
        remote.getCheckins().map { it.toDomain() }
    }

    override suspend fun getRooms(): AppResult<List<RoomStatus>> = safeApiCall {
        remote.getRooms().map { it.toDomain() }
    }

    override suspend fun confirmVacated(
        roomId: String,
        delegationId: String,
        notes: String?,
    ): AppResult<Unit> = when (
        val result = safeApiCall {
            remote.confirmVacated(roomId, ConfirmVacatedRequest(delegationId, notes?.takeIf { it.isNotBlank() }))
        }
    ) {
        is AppResult.Success ->
            if (result.value.success) AppResult.Success(Unit)
            else AppResult.Failure(AppError.Unknown("Vacate was not confirmed"))
        is AppResult.Failure -> result
    }

    private fun CheckInDto.toDomain(): CheckIn = CheckIn(
        id = id,
        bookingId = bookingId,
        roomId = roomId,
        startTime = startTime,
        endTime = endTime,
        slotType = StaffTime.slotFromWindow(startTime, endTime),
        active = status?.uppercase() == "ACTIVE",
    )

    private fun DashboardRoomDto.toDomain(): RoomStatus = RoomStatus(
        roomId = id,
        hotelName = hotel?.name ?: "Hotel",
        city = hotel?.city.orEmpty(),
        roomNumber = roomNumber ?: "—",
        roomType = roomType ?: "Room",
        currentStatus = currentStatus ?: "UNKNOWN",
        delegationId = activeDelegation?.id,
        endTime = activeDelegation?.endTime,
        timeLeftSeconds = timeLeft ?: Long.MAX_VALUE,
    )
}
