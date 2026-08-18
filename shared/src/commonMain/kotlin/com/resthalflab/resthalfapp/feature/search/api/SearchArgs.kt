package com.resthalflab.resthalfapp.feature.search.api

import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import kotlinx.serialization.Serializable

/**
 * The query that opens the results screen. Serializable so it can ride in the Decompose nav config.
 * [date] is ISO `yyyy-MM-dd` (matches the /search `date` param). [adults] is kept for the existing
 * day-room /search call; [occupancy] carries the full rooms/adults/children for the wholesale flow.
 */
@Serializable
data class SearchArgs(
    val city: String,
    val date: String,
    val slotType: SlotType,
    val adults: Int = 1,
    val nights: Int = 1,
    val occupancy: Occupancy = Occupancy(),
)
