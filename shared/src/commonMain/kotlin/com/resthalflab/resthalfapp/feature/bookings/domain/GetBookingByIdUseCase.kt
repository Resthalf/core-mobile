package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking

class GetBookingByIdUseCase(private val repository: BookingsRepository) {
    suspend operator fun invoke(id: String): AppResult<Booking> = repository.getBookingById(id)
}
