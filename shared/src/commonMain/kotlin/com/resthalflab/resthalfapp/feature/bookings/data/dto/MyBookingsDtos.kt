package com.resthalflab.resthalfapp.feature.bookings.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MyBookingsResponse(
    val direct: List<BookingDto> = emptyList(),
    val wholesale: List<BookingDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class BookingDto(
    val id: String,
    val roomId: String? = null,
    val room: BookingRoomDto? = null,
    val slotType: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: String,
    val currency: String,
    val status: String? = null,
    val midtransOrderId: String? = null,
    val createdAt: String? = null,
    val delegation: DelegationDto? = null,
)

@Serializable
data class BookingRoomDto(
    val id: String,
    val roomNumber: String? = null,
    val roomType: String? = null,
    val hotel: BookingHotelDto? = null,
)

@Serializable
data class BookingHotelDto(
    val id: String,
    val name: String,
    val city: String? = null,
)

@Serializable
data class DelegationDto(
    val id: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,
)
