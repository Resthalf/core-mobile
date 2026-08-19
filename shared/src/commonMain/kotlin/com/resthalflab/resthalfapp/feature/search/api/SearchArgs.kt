package com.resthalflab.resthalfapp.feature.search.api

import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import kotlinx.serialization.Serializable

/**
 * The query that opens the results screen. Serializable so it can ride in the Decompose nav config.
 *
 * Nexus hotel search uses [location] + [checkIn]/[checkOut] (ISO yyyy-MM-dd) + [occupancy]. The
 * legacy day-room fields ([date]/[slotType]/[adults]/[nights]) are kept for the offline fallback.
 */
@Serializable
data class SearchArgs(
    val city: String,
    val date: String,
    val slotType: SlotType,
    val adults: Int = 1,
    val nights: Int = 1,
    val occupancy: Occupancy = Occupancy(),
    val location: LocationSuggestion? = null,
    val checkIn: String = "",
    val checkOut: String = "",
)
