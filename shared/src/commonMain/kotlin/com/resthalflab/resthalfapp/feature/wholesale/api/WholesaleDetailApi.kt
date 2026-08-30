package com.resthalflab.resthalfapp.feature.wholesale.api

import com.resthalflab.resthalfapp.core.domain.AppResult

/**
 * Loads a hotel's detail page: hotel content (best-effort) + rooms & rates for the given search
 * [searchToken]. [checkIn]/[checkOut] (ISO yyyy-MM-dd) are used to derive the per-night price.
 */
interface WholesaleDetailApi {
    suspend fun loadDetail(
        hotelId: String,
        searchToken: String,
        checkIn: String,
        checkOut: String,
    ): AppResult<WholesaleHotelDetail>
}
