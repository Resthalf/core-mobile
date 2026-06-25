package com.resthalflab.resthalfapp.feature.bookings.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.model.CancelPreview

class GetCancelPreviewUseCase(private val repository: BookingsRepository) {
    suspend operator fun invoke(bookingId: String): AppResult<CancelPreview> =
        repository.cancelPreview(bookingId)
}

class CancelBookingUseCase(private val repository: BookingsRepository) {
    suspend operator fun invoke(bookingId: String, reason: String): AppResult<Unit> =
        repository.cancel(bookingId, reason)
}
