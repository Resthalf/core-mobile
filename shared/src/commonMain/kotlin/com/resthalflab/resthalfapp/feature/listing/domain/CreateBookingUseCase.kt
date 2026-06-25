package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.listing.data.BookingRemote
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectRequest
import com.resthalflab.resthalfapp.feature.listing.data.dto.BookingDirectResponse

class CreateBookingUseCase(
    private val remote: BookingRemote,
) {
    /**
     * Books the exact window the backend offered for the searched date ([startTime]/[endTime] come
     * straight from the /search result), so the booking honours the date the guest picked.
     */
    suspend operator fun invoke(
        roomId: String,
        slotType: String,
        startTime: String,
        endTime: String,
    ): AppResult<BookingDirectResponse> =
        safeApiCall {
            remote.createDirect(
                BookingDirectRequest(
                    roomId = roomId,
                    startTime = startTime,
                    endTime = endTime,
                    slotType = slotType,
                )
            )
        }
}
