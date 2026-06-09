package com.resthalflab.resthalfapp.feature.search.domain.model

import com.resthalflab.resthalfapp.feature.search.api.SlotType

/** A single bookable room offer within a hotel result, for the chosen slot type. */
data class RoomOption(
    val roomId: String,
    val roomNumber: String,
    val slotType: SlotType,
    val price: Int,
    val currency: String,
)
