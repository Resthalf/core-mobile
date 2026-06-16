package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.serialization.Serializable

/** Result of a successful /bookings/direct, carried into the confirmation screen. */
@Serializable
data class BookingConfirmationArgs(
    val bookingId: String,
    val orderId: String,
    val hotelName: String,
    val roomNumber: String,
    val slotType: String,
    val amount: Int,
    val currency: String,
)
