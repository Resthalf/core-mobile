package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.bookings.data.dto.BookingDto
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus

class DefaultBookingsRepository(
    private val remote: BookingsRemote,
) : BookingsRepository {

    override suspend fun getBookings(): AppResult<List<Booking>> = safeApiCall {
        remote.getMyBookings().direct.map { it.toDomain() }
    }

    override suspend fun getBookingById(id: String): AppResult<Booking> =
        when (val result = safeApiCall { remote.getMyBookings() }) {
            is AppResult.Success ->
                result.value.direct.firstOrNull { it.id == id }?.toDomain()
                    ?.let { AppResult.Success(it) }
                    ?: AppResult.Failure(AppError.Unknown("Booking not found"))
            is AppResult.Failure -> result
        }

    private fun BookingDto.toDomain(): Booking {
        // Status: PENDING from the booking status field (payment incomplete); otherwise Active when a
        // delegation exists, else Completed. (Cancelled/Overstayed not derivable yet.)
        val bookingStatus = when {
            status?.uppercase() == "PENDING" -> BookingStatus.Pending
            delegation != null -> BookingStatus.Active
            else -> BookingStatus.Completed
        }
        val windowStart = delegation?.startTime ?: startTime
        val windowEnd = delegation?.endTime ?: endTime
        return Booking(
            id = id,
            bookingCode = midtransOrderId ?: id,
            hotelName = room?.hotel?.name ?: "Hotel",
            city = room?.hotel?.city.orEmpty(),
            dateLabel = BookingTime.formatDate(windowStart),
            stayWindow = "${BookingTime.formatClock12h(windowStart)} – ${BookingTime.formatClock12h(windowEnd)}",
            totalPrice = totalPrice.toAmount(),
            currency = currency,
            status = bookingStatus,
            thumbnailUrl = null,
            roomNumber = room?.roomNumber ?: "—",
            slotType = slotType,
            startTime = windowStart,
            endTime = windowEnd,
        )
    }

    // "250000.00" -> 250000
    private fun String.toAmount(): Int =
        substringBefore('.').toIntOrNull() ?: toDoubleOrNull()?.toInt() ?: 0
}
