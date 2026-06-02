package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking

interface BookingsRepository {
    suspend fun getBookings(): AppResult<List<Booking>>
    suspend fun getBookingById(id: String): AppResult<Booking>
}
