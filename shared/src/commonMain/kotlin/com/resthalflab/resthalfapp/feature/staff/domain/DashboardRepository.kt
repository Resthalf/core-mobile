package com.resthalflab.resthalfapp.feature.staff.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.staff.domain.model.CheckIn
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus

interface DashboardRepository {
    suspend fun getCheckins(): AppResult<List<CheckIn>>
    suspend fun getRooms(): AppResult<List<RoomStatus>>
    suspend fun confirmVacated(roomId: String, delegationId: String, notes: String?): AppResult<Unit>
}
