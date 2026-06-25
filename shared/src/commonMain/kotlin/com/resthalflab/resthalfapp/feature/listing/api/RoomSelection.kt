package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.serialization.Serializable

/**
 * The room a guest picked from search results — the input to the listing/booking detail screen.
 * [slotType] is the raw API value ("HALF_DAY" / "FULL_DAY") to keep this contract independent of
 * the search feature's enum.
 */
@Serializable
data class RoomSelection(
    val roomId: String,
    val roomNumber: String,
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val slotType: String,
    val price: Int,
    val currency: String,
    // ISO stay window for the searched date, sourced from the /search offer and sent to /bookings/direct.
    val startTime: String,
    val endTime: String,
)
