package com.resthalflab.resthalfapp.feature.wholesale.api

import com.resthalflab.resthalfapp.core.domain.AppResult

/**
 * Zentrumhub hotel availability search. Orchestrates the Nexus flow behind one call:
 * (city/region → Get Location → polygon) or (else → circular) → Search Init → poll Results.
 */
interface WholesaleSearchApi {
    suspend fun searchHotels(
        location: LocationSuggestion,
        checkIn: String, // ISO yyyy-MM-dd
        checkOut: String, // ISO yyyy-MM-dd
        occupancy: Occupancy,
    ): AppResult<List<WholesaleHotel>>
}
