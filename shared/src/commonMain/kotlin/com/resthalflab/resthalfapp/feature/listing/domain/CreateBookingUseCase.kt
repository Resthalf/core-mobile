package com.resthalflab.resthalfapp.feature.listing.domain

import com.benasher44.uuid.uuid4
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.api.LocalBookingStore
import com.resthalflab.resthalfapp.feature.bookings.api.StoredBooking
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectResponse
import kotlin.time.Clock

/**
 * Creates a Pending booking on-device (RestHalf backend retired). The stay window comes straight
 * from the search offer carried in [RoomSelection], so the booking honours the searched date.
 */
class CreateBookingUseCase(
    private val store: LocalBookingStore,
) {
    suspend operator fun invoke(selection: RoomSelection): AppResult<BookingDirectResponse> = try {
        val id = uuid4().toString()
        val orderId = "RH-${Clock.System.now().toEpochMilliseconds()}"
        store.upsert(
            StoredBooking(
                id = id,
                bookingCode = orderId,
                hotelId = selection.hotelId,
                hotelName = selection.hotelName,
                city = selection.city,
                roomId = selection.roomId,
                roomNumber = selection.roomNumber,
                slotType = selection.slotType,
                totalPrice = selection.price,
                currency = selection.currency,
                startTime = selection.startTime,
                endTime = selection.endTime,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
        AppResult.Success(BookingDirectResponse(bookingId = id, orderId = orderId, amount = selection.price))
    } catch (t: Throwable) {
        AppResult.Failure(AppError.Unknown("Couldn't create booking", t))
    }
}
