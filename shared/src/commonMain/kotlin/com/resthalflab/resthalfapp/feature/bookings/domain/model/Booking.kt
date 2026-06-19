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
    val roomNumber: String,
    val slotType: String,
    // ISO-8601 window used for the active countdown (delegation window when active).
    val startTime: String,
    val endTime: String,
)

enum class BookingStatus { Pending, Active, Completed, Cancelled, Overstayed }
