package com.resthalflab.resthalfapp.feature.search.domain.model

import com.resthalflab.resthalfapp.feature.search.api.SlotType

/** A single bookable room offer within a hotel result, for the chosen slot type. */
data class RoomOption(
    val roomId: String,
    val roomNumber: String,
    val slotType: SlotType,
    val price: Int,
    val currency: String,
    // The backend-offered stay window for the searched date — carried into the booking so it
    // reflects the date the guest picked (not "now").
    val startTime: String,
    val endTime: String,
)
