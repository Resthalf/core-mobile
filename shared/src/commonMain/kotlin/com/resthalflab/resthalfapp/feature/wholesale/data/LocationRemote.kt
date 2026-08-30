package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.feature.wholesale.data.dto.AutosuggestResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.LocationContentDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Uses the Zentrumhub autosuggest client (base URL preset, no credentials required). */
class LocationRemote(
    private val client: HttpClient,
) {
    suspend fun autosuggest(term: String): AutosuggestResponseDto =
        client.get("api/locations/locationcontent/autosuggest") {
            parameter("term", term)
        }.body()

    /** Boundary polygon + details for a location id — used to build a polygonal region search. */
    suspend fun getLocation(id: String): LocationContentDto =
        client.get("api/locations/locationcontent/location/$id") {
            parameter("getSublocations", false)
        }.body()
}
