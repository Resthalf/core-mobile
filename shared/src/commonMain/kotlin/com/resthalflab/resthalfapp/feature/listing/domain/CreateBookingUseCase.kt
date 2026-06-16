package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.listing.data.BookingRemote
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectRequest
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectResponse
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class CreateBookingUseCase(
    private val remote: BookingRemote,
) {
    /**
     * startTime = now; endTime = now + the slot's max window (12h for HALF_DAY, 24h for FULL_DAY).
     */
    suspend operator fun invoke(roomId: String, slotType: String): AppResult<BookingDirectResponse> =
        safeApiCall {
            val start = Clock.System.now()
            val end = start + (if (slotType == "FULL_DAY") 24.hours else 12.hours)
            remote.createDirect(
                BookingDirectRequest(
                    roomId = roomId,
                    startTime = start.toString(),
                    endTime = end.toString(),
                    slotType = slotType,
                )
            )
        }
}
