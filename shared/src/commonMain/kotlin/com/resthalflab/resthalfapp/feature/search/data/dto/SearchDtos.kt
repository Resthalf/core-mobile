package com.resthalflab.resthalfapp.feature.search.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val direct: List<RoomOfferDto> = emptyList(),
    val wholesale: List<RoomOfferDto> = emptyList(),
    val meta: SearchMetaDto? = null,
)

@Serializable
data class RoomOfferDto(
    val source: String? = null,
    val roomId: String,
    val roomNumber: String? = null,
    val hotel: HotelDto,
    val slotType: String,
    val startTime: String,
    val endTime: String,
    val price: Int,
    val currency: String,
    val label: String? = null,
    val badge: String? = null,
)

@Serializable
data class HotelDto(
    val id: String,
    val name: String,
    val city: String,
)

@Serializable
data class SearchMetaDto(
    val directCount: Int = 0,
    val wholesaleCount: Int = 0,
)
