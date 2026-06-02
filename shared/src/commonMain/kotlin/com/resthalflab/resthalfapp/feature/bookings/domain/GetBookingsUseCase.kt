package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking

class GetBookingsUseCase(private val repository: BookingsRepository) {
    suspend operator fun invoke(): AppResult<List<Booking>> = repository.getBookings()
}
