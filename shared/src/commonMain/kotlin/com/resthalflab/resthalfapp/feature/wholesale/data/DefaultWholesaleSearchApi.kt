package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.ZentrumhubConfig
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleSearchResult
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.CircularRegionDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.CoordinatesDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.OccupancyDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.PolygonalRegionDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchInitRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchLocationDetailsDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchResultsDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.toWholesaleHotel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Runs the Zentrumhub search pipeline: pick the search mode from the location type (city/region →
 * Get Location → polygon; else → circular), then in parallel init+poll the availability search and
 * fetch hotel content (names/images/ratings), merging the two by hotel id.
 */
class DefaultWholesaleSearchApi(
    private val locationRemote: LocationRemote,
    private val nexusRemote: NexusRemote,
    private val config: ZentrumhubConfig,
) : WholesaleSearchApi {

    override suspend fun searchHotels(
        location: LocationSuggestion,
        checkIn: String,
        checkOut: String,
        occupancy: Occupancy,
    ): AppResult<WholesaleSearchResult> = safeApiCall {
        withContext(Dispatchers.Default) {
            val nights = nightsBetween(checkIn, checkOut)
            val (polygon, circular) = buildRegion(location)

            val token = nexusRemote.searchInit(
                initRequest(location, checkIn, checkOut, occupancy, polygon, circular)
            ).token
            val results = pollResults(token)
            val currency = results.currency?.takeIf { it.isNotBlank() } ?: CURRENCY
            // The token used to fetch rooms & rates for a hotel — prefer the one echoed by results.
            val searchToken = results.token?.takeIf { it.isNotBlank() } ?: token

            // Content is looked up by the ids the search returned (content-by-region 204s). Best-effort:
            // a failure/empty just leaves names/images blank rather than dropping the results.
            val ids = results.hotels.map { it.id }
            val content: Map<String, HotelContentDto> = if (ids.isEmpty()) {
                emptyMap()
            } else {
                runCatching {
                    nexusRemote.getHotelContent(contentRequest(ids)).hotels.associateBy { it.id }
                }.getOrElse { emptyMap() }
            }

            val hotels = results.hotels
                .filter { it.rate != null }
                .map { it.toWholesaleHotel(currency = currency, nights = nights, content = content[it.id]) }
                .sortedBy { it.perNightRate }

            WholesaleSearchResult(token = searchToken, hotels = hotels)
        }
    }

    private suspend fun buildRegion(location: LocationSuggestion): Pair<PolygonalRegionDto?, CircularRegionDto?> {
        if (location.apiType.lowercase() in POLYGON_TYPES) {
            val ring = locationRemote.getLocation(location.id).boundaries.firstOrNull().orEmpty()
            if (ring.isNotEmpty()) return PolygonalRegionDto(ring) to null
        }
        return null to CircularRegionDto(
            centerLat = location.coordinates.lat,
            centerLong = location.coordinates.long,
            radiusInKm = DEFAULT_RADIUS_KM,
        )
    }

    private fun initRequest(
        location: LocationSuggestion,
        checkIn: String,
        checkOut: String,
        occupancy: Occupancy,
        polygon: PolygonalRegionDto?,
        circular: CircularRegionDto?,
    ): SearchInitRequestDto = SearchInitRequestDto(
        channelId = config.channelId,
        currency = CURRENCY,
        culture = CULTURE,
        checkIn = checkIn,
        checkOut = checkOut,
        occupancies = List(occupancy.rooms.coerceAtLeast(1)) {
            OccupancyDto(
                numOfAdults = occupancy.adults.coerceAtLeast(1),
                childAges = List(occupancy.children.coerceAtLeast(0)) { CHILD_AGE_DEFAULT },
            )
        },
        polygonalRegion = polygon,
        circularRegion = circular,
        searchLocationDetails = locationDetails(location),
        nationality = NATIONALITY,
        countryOfResidence = NATIONALITY,
        destinationCountryCode = location.country.ifBlank { NATIONALITY },
    )

    private fun contentRequest(hotelIds: List<String>): HotelContentRequestDto = HotelContentRequestDto(
        channelId = config.channelId,
        culture = CONTENT_CULTURE,
        hotelIds = hotelIds,
        contentFields = CONTENT_FIELDS,
    )

    private fun locationDetails(location: LocationSuggestion) = SearchLocationDetailsDto(
        id = location.id,
        name = location.name,
        fullName = location.fullName,
        type = location.apiType.ifBlank { "City" },
        state = location.state,
        country = location.country.ifBlank { NATIONALITY },
        coordinates = CoordinatesDto(location.coordinates.lat, location.coordinates.long),
    )

    private suspend fun pollResults(token: String): SearchResultsDto {
        var last = SearchResultsDto()
        repeat(MAX_POLLS) { attempt ->
            last = nexusRemote.searchResults(token)
            if (last.status.equals("Completed", ignoreCase = true)) return last
            // Return an early batch so the user isn't blocked waiting for every hotel to complete.
            if (last.hotels.isNotEmpty() && attempt >= EARLY_RETURN_AFTER) return last
            delay(POLL_DELAY_MS)
        }
        return last
    }

    private fun nightsBetween(checkIn: String, checkOut: String): Int = runCatching {
        LocalDate.parse(checkIn).daysUntil(LocalDate.parse(checkOut)).coerceAtLeast(1)
    }.getOrDefault(1)

    private companion object {
        val POLYGON_TYPES = setOf("city", "multicity", "region")
        val CONTENT_FIELDS = listOf(
            "Basic", "Facilities", "Descriptions", "Images", "Reviews", "Neighbourhoods", "Policies",
        )
        const val CURRENCY = "IDR"
        const val CULTURE = "id-ID"
        const val CONTENT_CULTURE = "en-US"
        const val NATIONALITY = "ID"
        const val DEFAULT_RADIUS_KM = 30
        const val CHILD_AGE_DEFAULT = 8
        const val MAX_POLLS = 8
        const val EARLY_RETURN_AFTER = 2
        const val POLL_DELAY_MS = 1000L
    }
}
