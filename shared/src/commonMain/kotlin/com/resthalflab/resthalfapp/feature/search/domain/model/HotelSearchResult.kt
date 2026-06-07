package com.resthalflab.resthalfapp.feature.search.domain.model

import com.resthalflab.resthalfapp.feature.search.api.SlotType

/** One hotel in the results list — the cheapest available room of the chosen slot type, with count. */
data class HotelSearchResult(
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val slotType: SlotType,
    val slotLabel: String,
    val badge: String?,
    val windowLabel: String,
    val fromPrice: Int,
    val currency: String,
    val roomCount: Int,
)
