package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.VacateResult

interface BookingsRepository {
    suspend fun getBookings(): AppResult<List<Booking>>
    suspend fun getBookingById(id: String): AppResult<Booking>
    suspend fun vacate(delegationId: String): AppResult<VacateResult>
}
