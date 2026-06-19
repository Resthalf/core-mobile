package com.resthalflab.resthalfapp.feature.staff.domain.model

import com.resthalflab.resthalfapp.feature.search.api.SlotType

/** A staff-facing view of a guest delegation (check-in). */
data class CheckIn(
    val id: String,
    val bookingId: String,
    val roomId: String,
    val startTime: String,
    val endTime: String,
    val slotType: SlotType,
    /** True while the delegation is ongoing (status == ACTIVE). */
    val active: Boolean,
)
