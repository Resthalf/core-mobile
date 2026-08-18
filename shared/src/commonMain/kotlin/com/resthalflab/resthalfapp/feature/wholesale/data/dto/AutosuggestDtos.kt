package com.resthalflab.resthalfapp.feature.wholesale.data.dto

import com.resthalflab.resthalfapp.feature.wholesale.api.Coordinates
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutosuggestResponseDto(
    val locationSuggestions: List<LocationSuggestionDto> = emptyList(),
    val status: String? = null,
)

@Serializable
data class CoordinatesDto(
    val lat: Double = 0.0,
    @SerialName("long") val long: Double = 0.0,
)

@Serializable
data class LocationSuggestionDto(
    val id: String,
    val name: String,
    val fullName: String? = null,
    val type: String? = null,
    val code: String? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val coordinates: CoordinatesDto? = null,
    val referenceId: String? = null,
    val referenceScore: Long = 0,
)

fun LocationSuggestionDto.toDomain(): LocationSuggestion = LocationSuggestion(
    id = id,
    name = name,
    fullName = fullName ?: name,
    type = type.toLocationType(),
    country = country.orEmpty(),
    coordinates = coordinates?.let { Coordinates(it.lat, it.long) } ?: Coordinates(0.0, 0.0),
    code = code,
    city = city,
    state = state,
    referenceId = referenceId,
    referenceScore = referenceScore,
)

private fun String?.toLocationType(): LocationType = when (this?.lowercase()) {
    "city" -> LocationType.CITY
    "state" -> LocationType.STATE
    "airport" -> LocationType.AIRPORT
    "hotel" -> LocationType.HOTEL
    "area" -> LocationType.AREA
    else -> LocationType.UNKNOWN
}
