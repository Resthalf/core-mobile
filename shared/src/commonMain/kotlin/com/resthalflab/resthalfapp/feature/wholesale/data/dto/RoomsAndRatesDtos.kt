package com.resthalflab.resthalfapp.feature.wholesale.data.dto

import kotlinx.serialization.Serializable

// ---- Rooms & Rates (Nexus: POST hotel/{hotelId}/roomsandrates/{token}) --------------------------
// We only model the fields the Room Details UI needs: the standardized rooms (name/beds/facilities)
// and the standardized room groups (price options + rate ids). Provider rooms/rates/recommendations
// are left unmodelled — the client ignores unknown keys.

@Serializable
data class RoomsAndRatesRequestDto(val searchSpecificProviders: Boolean = false)

@Serializable
data class RoomsAndRatesResponseDto(
    val token: String? = null,
    val currency: String? = null,
    val hotel: RoomsHotelDto? = null,
)

@Serializable
data class RoomsHotelDto(
    val id: String? = null,
    val standardizedRooms: List<StandardizedRoomDto> = emptyList(),
    val standardizedRoomGroups: List<StandardizedRoomGroupDto> = emptyList(),
)

@Serializable
data class StandardizedRoomDto(
    val id: String,
    val name: String? = null,
    val bedInfo: String? = null,
    val maxGuestAllowed: String? = null,
    val maxAdultAllowed: String? = null,
    val maxChildrenAllowed: String? = null,
    val facilities: List<RoomFacilityDto> = emptyList(),
    val images: List<RoomImageDto> = emptyList(),
    val views: List<String> = emptyList(),
    val description: String? = null,
    val mappedRoomRates: List<MappedRoomRateDto> = emptyList(),
)

@Serializable
data class RoomFacilityDto(val name: String? = null)

@Serializable
data class RoomImageDto(val links: List<RoomImageLinkDto> = emptyList())

@Serializable
data class RoomImageLinkDto(val size: String? = null, val url: String? = null)

/** Maps a rate id (used in the group options) to its board basis + refundability. */
@Serializable
data class MappedRoomRateDto(
    val inputIndex: String? = null,
    val roomCode: String? = null,
    val boardBasis: String? = null,
    val refundability: String? = null,
    val rateId: String? = null,
)

@Serializable
data class StandardizedRoomGroupDto(
    val standardRoomIds: List<String> = emptyList(),
    val options: List<RoomGroupOptionDto> = emptyList(),
)

@Serializable
data class RoomGroupOptionDto(
    val recommendationId: String? = null,
    val totalRate: Double = 0.0,
    val standardRooms: List<OptionStandardRoomDto> = emptyList(),
)

@Serializable
data class OptionStandardRoomDto(
    val standardRoomId: String? = null,
    val totalRate: Double = 0.0,
    val rateIds: List<String> = emptyList(),
)
