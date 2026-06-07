package com.resthalflab.resthalfapp.feature.search.data

import com.resthalflab.resthalfapp.feature.search.data.dto.SearchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Uses the default (authenticated) client — the Bearer token from login is attached automatically. */
class SearchRemote(
    private val client: HttpClient,
) {
    suspend fun search(
        city: String,
        date: String,
        nights: Int,
        adults: Int,
        includeWholesale: Boolean,
    ): SearchResponseDto =
        client.get("search") {
            parameter("city", city)
            parameter("date", date)
            parameter("nights", nights)
            parameter("adults", adults)
            parameter("includeWholesale", includeWholesale)
        }.body()
}
