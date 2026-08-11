package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.core.storage.FailedBookingStore
import com.resthalflab.resthalfapp.feature.bookings.data.dto.BookingDto
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import com.resthalflab.resthalfapp.feature.bookings.domain.model.CancelPreview
import com.resthalflab.resthalfapp.feature.bookings.domain.model.VacateResult

class DefaultBookingsRepository(
    private val remote: BookingsRemote,
    private val failedBookingStore: FailedBookingStore,
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

    override suspend fun vacate(delegationId: String): AppResult<VacateResult> = when (
        val result = safeApiCall { remote.vacate(delegationId) }
    ) {
        is AppResult.Success ->
            if (result.value.success) {
                AppResult.Success(
                    VacateResult(
                        message = result.value.message ?: "Room released.",
                        earlyByMinutes = result.value.earlyByMinutes,
                    )
                )
            } else {
                AppResult.Failure(AppError.Unknown("Could not release the room"))
            }
        is AppResult.Failure -> result
    }

    override suspend fun cancelPreview(bookingId: String): AppResult<CancelPreview> = safeApiCall {
        remote.cancelPreview(bookingId).let {
            CancelPreview(
                allowed = it.allowed,
                policyType = it.policyType.orEmpty(),
                originalAmount = it.originalAmount,
                refundAmount = it.refundAmount,
                refundPercent = it.refundPercent,
                deadlinePassed = it.deadlinePassed,
                reason = it.reason.orEmpty(),
            )
        }
    }

    override suspend fun cancel(bookingId: String, reason: String): AppResult<Unit> = when (
        val result = safeApiCall { remote.cancel(bookingId, reason) }
    ) {
        is AppResult.Success ->
            if (result.value.success) AppResult.Success(Unit)
            else AppResult.Failure(AppError.Unknown(result.value.reason ?: "Cancellation failed"))
        is AppResult.Failure -> result
    }

    override suspend fun reschedule(bookingId: String, newStart: String, newEnd: String): AppResult<Unit> = when (
        val result = safeApiCall { remote.reschedule(bookingId, newStart, newEnd) }
    ) {
        is AppResult.Success ->
            if (result.value.success) AppResult.Success(Unit)
            else AppResult.Failure(AppError.Unknown("Reschedule failed"))
        is AppResult.Failure -> result
    }

    private fun BookingDto.toDomain(): Booking {
        val windowStart = delegation?.startTime ?: startTime
        val windowEnd = delegation?.endTime ?: endTime
        // Status precedence: a client-flagged payment failure wins; then explicit backend CANCELLED /
        // PENDING. Active means checked-in (a live delegation) AND the window has actually begun — a
        // paid booking that's only upcoming (no delegation yet, or not started) stays Confirmed so it
        // shows a "starts in" countdown and hides the vacate action. Past the window → Completed.
        val bookingStatus = when {
            failedBookingStore.isFailed(id) -> BookingStatus.InternalError
            status?.uppercase() == "CANCELLED" -> BookingStatus.Cancelled
            status?.uppercase() == "PENDING" -> BookingStatus.Pending
            delegation != null && BookingTime.hasStarted(windowStart) && !BookingTime.hasEnded(windowEnd) ->
                BookingStatus.Active
            !BookingTime.hasEnded(windowEnd) -> BookingStatus.Confirmed
            else -> BookingStatus.Completed
        }
        return Booking(
            id = id,
            bookingCode = midtransOrderId ?: id,
            hotelName = room?.hotel?.name ?: "Hotel",
            city = room?.hotel?.city.orEmpty(),
            dateLabel = BookingTime.formatDate(windowStart),
            stayWindow = BookingTime.formatWindow(windowStart, windowEnd),
            totalPrice = totalPrice.toAmount(),
            currency = currency,
            status = bookingStatus,
            thumbnailUrl = null,
            roomNumber = room?.roomNumber ?: "—",
            slotType = slotType,
            startTime = windowStart,
            endTime = windowEnd,
            delegationId = delegation?.id,
        )
    }

    // "250000.00" -> 250000
    private fun String.toAmount(): Int =
        substringBefore('.').toIntOrNull() ?: toDoubleOrNull()?.toInt() ?: 0
}
