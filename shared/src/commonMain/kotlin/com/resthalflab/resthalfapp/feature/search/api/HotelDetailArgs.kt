package com.resthalflab.resthalfapp.feature.search.api

import kotlinx.serialization.Serializable

/**
 * Opens the hotel detail screen. Carries the search [searchToken] (needed for roomsandrates) plus
 * the stay window and a few header fields from the results card so the screen can render instantly
 * while the detail loads. Serializable so it can ride in the Decompose nav config.
 */
@Serializable
data class HotelDetailArgs(
    val hotelId: String,
    val hotelName: String,
    val heroImage: String? = null,
    val starRating: Int? = null,
    val reviewRating: Double? = null,
    val reviewCount: Int? = null,
    val address: String? = null,
    val category: String? = null,
    val currency: String = "IDR",
    val searchToken: String,
    val checkIn: String,
    val checkOut: String,
)
