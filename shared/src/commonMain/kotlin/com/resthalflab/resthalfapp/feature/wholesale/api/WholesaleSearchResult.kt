package com.resthalflab.resthalfapp.feature.wholesale.api

/**
 * Result of an availability search: the [token] identifies the search session (needed to fetch
 * rooms & rates for a specific hotel) and [hotels] are the mapped result cards.
 */
data class WholesaleSearchResult(
    val token: String,
    val hotels: List<WholesaleHotel>,
)
