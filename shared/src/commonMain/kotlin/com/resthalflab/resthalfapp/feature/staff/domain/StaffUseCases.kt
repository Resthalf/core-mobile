package com.resthalflab.resthalfapp.feature.staff.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.staff.domain.model.CheckIn
import com.resthalflab.resthalfapp.feature.staff.domain.model.RoomStatus

class GetCheckInsUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(): AppResult<List<CheckIn>> = repository.getCheckins()
}

class GetRoomsUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(): AppResult<List<RoomStatus>> = repository.getRooms()
}

class ConfirmVacateUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(roomId: String, delegationId: String, notes: String?): AppResult<Unit> =
        repository.confirmVacated(roomId, delegationId, notes)
}
