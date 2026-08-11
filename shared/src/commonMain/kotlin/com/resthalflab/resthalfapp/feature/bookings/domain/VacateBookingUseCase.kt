package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.VacateResult

class VacateBookingUseCase(private val repository: BookingsRepository) {
    suspend operator fun invoke(delegationId: String): AppResult<VacateResult> =
        repository.vacate(delegationId)
}
