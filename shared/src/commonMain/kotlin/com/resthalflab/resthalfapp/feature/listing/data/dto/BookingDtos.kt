package com.resthalflab.resthalfapp.feature.listing.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookingDirectRequest(
    val roomId: String,
    val startTime: String,
    val endTime: String,
    val slotType: String,
)

@Serializable
data class BookingDirectResponse(
    val bookingId: String,
    val orderId: String,
    val amount: Int,
)
