package com.resthalflab.resthalfapp.feature.wholesale.api

import com.resthalflab.resthalfapp.core.domain.AppResult

/**
 * Zentrumhub Nexus booking phase: re-price the chosen recommendation, hold the session (book init),
 * then confirm (book). [priceCheck] uses the roomsandrates [token]; book init / book reuse it.
 */
interface WholesaleBookingApi {
    suspend fun priceCheck(hotelId: String, token: String, recommendationId: String): AppResult<PriceQuote>
    suspend fun bookInit(request: BookingRequest): AppResult<BookingResult>
    suspend fun book(request: BookingRequest): AppResult<BookingResult>
}
