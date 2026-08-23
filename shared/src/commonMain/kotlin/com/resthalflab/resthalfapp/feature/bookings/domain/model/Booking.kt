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
    // Active delegation id — required to vacate the room. Null when there's no active stay.
    val delegationId: String? = null,
    val tags: List<String> = emptyList(),
    val guestsLabel: String = "",
)

enum class BookingStatus { Pending, Confirmed, Active, Completed, Cancelled, Overstayed, InternalError }
