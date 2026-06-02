package com.resthalflab.resthalfapp.feature.bookings.domain.model

data class Booking(
    val id: String,
    val bookingCode: String,
    val hotelName: String,
    val city: String,
    val dateLabel: String,
    val stayWindow: String,
    val totalPrice: Int,
    val currency: String,
    val status: BookingStatus,
    val thumbnailUrl: String?,
)

enum class BookingStatus { Active, Completed, Cancelled, Overstayed }
