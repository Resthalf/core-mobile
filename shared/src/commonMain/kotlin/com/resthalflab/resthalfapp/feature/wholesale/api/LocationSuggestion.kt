package com.resthalflab.resthalfapp.feature.wholesale.api

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(val lat: Double, val long: Double)

/** The kind of place a suggestion points at. Drives the row icon and the slot-based filter. */
@Serializable
enum class LocationType { CITY, STATE, AIRPORT, HOTEL, AREA, UNKNOWN }

/**
 * A place the user can search for hotels in — a city, area, airport, or a specific property.
 * Serializable so a selected location can ride in the Decompose nav config in later wholesale steps.
 */
@Serializable
data class LocationSuggestion(
    val id: String,
    val name: String,
    val fullName: String,
    val type: LocationType,
    val country: String,
    val coordinates: Coordinates,
    /** Airport code, when [type] is [LocationType.AIRPORT]. */
    val code: String? = null,
    /** Parent city, present for hotels. */
    val city: String? = null,
    val state: String? = null,
    /** Provider-side hotel id, present for hotels. */
    val referenceId: String? = null,
    val referenceScore: Long = 0,
)
