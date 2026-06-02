package com.resthalflab.resthalfapp.feature.listing.api

data class BookingConfirmation(
    val bookingId: String,
    val hotelName: String,
    val roomType: String,
    val dateLabel: String,
    val stayWindow: String,
    val guestsLabel: String,
    val totalPaid: String,
)
