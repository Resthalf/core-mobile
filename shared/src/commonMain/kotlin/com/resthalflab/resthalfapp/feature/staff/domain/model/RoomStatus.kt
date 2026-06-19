package com.resthalflab.resthalfapp.feature.staff.domain.model

/** A staff-facing room with its current occupancy and active delegation (if any). */
data class RoomStatus(
    val roomId: String,
    val hotelName: String,
    val city: String,
    val roomNumber: String,
    val roomType: String,
    /** Raw backend status, e.g. OCCUPIED / AVAILABLE. */
    val currentStatus: String,
    /** Active delegation id — required to confirm a vacate. Null when the room is free. */
    val delegationId: String?,
    /** ISO end time of the active stay, used for the live countdown. Null when free. */
    val endTime: String?,
    /** Seconds remaining as reported by the backend (used for ordering "next vacated"). */
    val timeLeftSeconds: Long,
) {
    val isOccupied: Boolean get() = delegationId != null && endTime != null
}
