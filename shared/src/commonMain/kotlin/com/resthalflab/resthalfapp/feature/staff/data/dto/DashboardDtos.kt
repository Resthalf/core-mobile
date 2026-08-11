package com.resthalflab.resthalfapp.feature.staff.data.dto

import kotlinx.serialization.Serializable

/** GET /dashboard/checkins → flat list of delegations. */
@Serializable
data class CheckInDto(
    val id: String,
    val roomId: String,
    val bookingId: String,
    val startTime: String,
    val endTime: String,
    val status: String? = null,
    val vacatedAt: String? = null,
    val reconciledAt: String? = null,
    val expiredAt: String? = null,
)

/** GET /dashboard/rooms → rooms with their active delegation and remaining seconds. */
@Serializable
data class DashboardRoomDto(
    val id: String,
    val roomNumber: String? = null,
    val roomType: String? = null,
    val hotel: DashboardHotelDto? = null,
    val currency: String? = null,
    val currentStatus: String? = null,
    val activeDelegation: DashboardDelegationDto? = null,
    val expiredDelegation: DashboardDelegationDto? = null,
    val timeLeft: Long? = null,
)

@Serializable
data class DashboardHotelDto(
    val id: String,
    val name: String,
    val city: String? = null,
)

@Serializable
data class DashboardDelegationDto(
    val id: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,
)

/** POST /dashboard/rooms/{roomId}/confirm-vacated */
@Serializable
data class ConfirmVacatedRequest(
    val delegationId: String,
    val notes: String? = null,
)

@Serializable
data class ConfirmVacatedResponse(
    val success: Boolean = false,
)
