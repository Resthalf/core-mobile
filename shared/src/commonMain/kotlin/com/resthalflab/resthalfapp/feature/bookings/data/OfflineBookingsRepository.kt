package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.api.LocalBookingStore
import com.resthalflab.resthalfapp.feature.bookings.api.StoredBooking
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import com.resthalflab.resthalfapp.feature.bookings.domain.model.CancelPreview
import com.resthalflab.resthalfapp.feature.bookings.domain.model.VacateResult
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Bookings backed by the on-device [LocalBookingStore] (RestHalf backend retired). Status, refund
 * policy, and the active countdown are derived locally; the Zentrumhub Nexus-backed repo will
 * replace this per its spec, reusing the same [BookingsRepository] contract.
 */
class OfflineBookingsRepository(
    private val store: LocalBookingStore,
) : BookingsRepository {

    override suspend fun getBookings(): AppResult<List<Booking>> =
        AppResult.Success(store.bookings.value.sortedByDescending { it.createdAt }.map { it.toDomain() })

    override suspend fun getBookingById(id: String): AppResult<Booking> =
        store.get(id)?.toDomain()?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.Unknown("Booking not found"))

    override suspend fun vacate(delegationId: String): AppResult<VacateResult> {
        val booking = store.get(delegationId)
            ?: return AppResult.Failure(AppError.Unknown("Booking not found"))
        val now = Clock.System.now()
        val earlyByMinutes = runCatching {
            maxOf(0L, (Instant.parse(booking.endTime) - now).inWholeMinutes)
        }.getOrDefault(0L).toInt()
        store.vacate(delegationId, now.toString())
        return AppResult.Success(VacateResult(message = "Room released.", earlyByMinutes = earlyByMinutes))
    }

    override suspend fun cancelPreview(bookingId: String): AppResult<CancelPreview> {
        val b = store.get(bookingId)
            ?: return AppResult.Failure(AppError.Unknown("Booking not found"))
        val started = BookingTime.hasStarted(b.startTime)
        val refundPercent = when {
            !b.paid -> 0
            !started -> 100
            else -> 0
        }
        return AppResult.Success(
            CancelPreview(
                allowed = !started,
                policyType = if (started) "NON_REFUNDABLE" else "FREE_CANCELLATION",
                originalAmount = b.totalPrice,
                refundAmount = b.totalPrice * refundPercent / 100,
                refundPercent = refundPercent,
                deadlinePassed = started,
                reason = if (started) {
                    "The stay window has already started."
                } else {
                    "Free cancellation before check-in."
                },
            )
        )
    }

    override suspend fun cancel(bookingId: String, reason: String): AppResult<Unit> {
        store.get(bookingId) ?: return AppResult.Failure(AppError.Unknown("Booking not found"))
        store.cancel(bookingId)
        return AppResult.Success(Unit)
    }

    override suspend fun reschedule(bookingId: String, newStart: String, newEnd: String): AppResult<Unit> {
        store.get(bookingId) ?: return AppResult.Failure(AppError.Unknown("Booking not found"))
        store.reschedule(bookingId, newStart, newEnd)
        return AppResult.Success(Unit)
    }

    private fun StoredBooking.toDomain(): Booking {
        val effectiveEnd = endedAt ?: endTime
        val status = when {
            cancelled -> BookingStatus.Cancelled
            !paid -> BookingStatus.Pending
            endedEarly -> BookingStatus.Completed
            BookingTime.hasStarted(startTime) && !BookingTime.hasEnded(effectiveEnd) -> BookingStatus.Active
            !BookingTime.hasEnded(effectiveEnd) -> BookingStatus.Confirmed
            else -> BookingStatus.Completed
        }
        return Booking(
            id = id,
            bookingCode = bookingCode,
            hotelName = hotelName,
            city = city,
            dateLabel = BookingTime.formatDate(startTime),
            stayWindow = BookingTime.formatWindow(startTime, effectiveEnd),
            totalPrice = totalPrice,
            currency = currency,
            status = status,
            thumbnailUrl = null,
            roomNumber = roomNumber,
            slotType = slotType,
            startTime = startTime,
            endTime = effectiveEnd,
            delegationId = if (status == BookingStatus.Active) id else null,
        )
    }
}
