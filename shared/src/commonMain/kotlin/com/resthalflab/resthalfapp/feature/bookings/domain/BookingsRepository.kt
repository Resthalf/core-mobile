package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.CancelPreview
import com.resthalflab.resthalfapp.feature.bookings.domain.model.VacateResult

interface BookingsRepository {
    suspend fun getBookings(): AppResult<List<Booking>>
    suspend fun getBookingById(id: String): AppResult<Booking>
    suspend fun vacate(delegationId: String): AppResult<VacateResult>
    suspend fun cancelPreview(bookingId: String): AppResult<CancelPreview>
    suspend fun cancel(bookingId: String, reason: String): AppResult<Unit>
    suspend fun reschedule(bookingId: String, newStart: String, newEnd: String): AppResult<Unit>
}
