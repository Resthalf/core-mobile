package com.resthalflab.resthalfapp.feature.wholesale.api

import kotlinx.serialization.Serializable

/**
 * Guests for a stay. Each [rooms] entry becomes one occupancy in the provider's availability call;
 * child ages (needed by availability) are captured in a later step — counts suffice for the planner.
 */
@Serializable
data class Occupancy(
    val rooms: Int = 1,
    val adults: Int = 1,
    val children: Int = 0,
) {
    companion object {
        const val MIN_ROOMS = 1
        const val MAX_ROOMS = 8
        const val MIN_ADULTS = 1
        const val MAX_ADULTS = 16
        const val MIN_CHILDREN = 0
        const val MAX_CHILDREN = 8
    }
}
