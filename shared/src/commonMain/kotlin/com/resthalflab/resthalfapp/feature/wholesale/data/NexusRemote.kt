package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BookRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BookResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.PriceCheckResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.RoomsAndRatesRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.RoomsAndRatesResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchInitRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchInitResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchResultsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Zentrumhub Nexus (authenticated) endpoints. The injected client already attaches accountId / apiKey
 * / correlationId / customer-ip via its defaultRequest, so calls here just carry the payload.
 */
class NexusRemote(
    private val client: HttpClient,
) {
    suspend fun searchInit(request: SearchInitRequestDto): SearchInitResponseDto =
        client.post("api/hotel/availability/init") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun searchResults(token: String): SearchResultsDto =
        client.get("api/hotel/availability/async/$token/results").body()

    /** Hotel content (names/images/star rating/address) for the same region as the search. */
    suspend fun getHotelContent(request: HotelContentRequestDto): HotelContentResponseDto =
        client.post("api/content/hotelcontent/getHotelContent") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    /** Rooms & rates for one hotel, keyed by the availability search [token]. */
    suspend fun roomsAndRates(
        hotelId: String,
        token: String,
        request: RoomsAndRatesRequestDto = RoomsAndRatesRequestDto(),
    ): RoomsAndRatesResponseDto =
        client.post("api/hotel/$hotelId/roomsandrates/$token") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    /** Re-price the chosen recommendation just before booking. */
    suspend fun priceCheck(
        hotelId: String,
        token: String,
        recommendationId: String,
    ): PriceCheckResponseDto =
        client.get("api/hotel/$hotelId/$token/price/recommendation/$recommendationId").body()

    /** Hold the booking session on the provider (pre-payment). */
    suspend fun bookInit(hotelId: String, token: String, request: BookRequestDto): BookResponseDto =
        client.post("api/hotel/$hotelId/$token/bookinit") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    /** Confirm the booking (post-payment). */
    suspend fun book(hotelId: String, token: String, request: BookRequestDto): BookResponseDto =
        client.post("api/hotel/$hotelId/$token/book") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
