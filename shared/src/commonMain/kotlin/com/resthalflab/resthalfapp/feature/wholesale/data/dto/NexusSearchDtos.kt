package com.resthalflab.resthalfapp.feature.wholesale.data.dto

import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.math.roundToInt

// ---- Get Location (autosuggest base) ------------------------------------------------------------

@Serializable
data class LocationContentDto(
    val shape: String? = null,
    val boundaries: List<List<CoordinatesDto>> = emptyList(),
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val coordinates: CoordinatesDto? = null,
)

// ---- Search init (Nexus) ------------------------------------------------------------------------

@Serializable
data class OccupancyDto(
    val numOfAdults: Int,
    val childAges: List<Int> = emptyList(),
)

@Serializable
data class PolygonalRegionDto(val coordinates: List<CoordinatesDto>)

@Serializable
data class CircularRegionDto(val centerLat: Double, val centerLong: Double, val radiusInKm: Int)

@Serializable
data class SearchLocationDetailsDto(
    val id: String,
    val name: String,
    val fullName: String,
    val type: String,
    val state: String? = null,
    val country: String,
    val coordinates: CoordinatesDto,
)

@Serializable
data class SearchInitRequestDto(
    val channelId: String,
    val currency: String,
    val culture: String,
    val checkIn: String,
    val checkOut: String,
    val occupancies: List<OccupancyDto>,
    val polygonalRegion: PolygonalRegionDto? = null,
    val circularRegion: CircularRegionDto? = null,
    val searchLocationDetails: SearchLocationDetailsDto,
    val nationality: String,
    val countryOfResidence: String,
    val destinationCountryCode: String,
    val travelPurpose: String = "Leisure",
    val filterBy: String? = null,
)

@Serializable
data class SearchInitResponseDto(val token: String)

// ---- Search results (Nexus, polled) -------------------------------------------------------------

@Serializable
data class SearchResultsDto(
    val token: String? = null,
    val nextResultsKey: String? = null,
    val status: String? = null,
    val expectedHotelCount: String? = null,
    val completedHotelCount: String? = null,
    val currency: String? = null,
    val hotels: List<HotelResultDto> = emptyList(),
)

@Serializable
data class HotelResultDto(
    val id: String,
    val rate: RateDto? = null,
    val options: OptionsDto? = null,
)

@Serializable
data class RateDto(
    val totalRate: Double = 0.0,
    val publishedRate: Double = 0.0,
    val baseRate: Double = 0.0,
    val taxes: Double = 0.0,
    val boardBasis: BoardBasisDto? = null,
    val refundability: String? = null,
    val offer: OfferDto? = null,
)

@Serializable
data class BoardBasisDto(val description: String? = null, val type: String? = null)

@Serializable
data class OfferDto(val title: String? = null, val description: String? = null)

@Serializable
data class OptionsDto(
    val freeBreakfast: Boolean = false,
    val freeCancellation: Boolean = false,
    val refundable: Boolean = false,
    val payAtHotel: Boolean = false,
    val roomOnly: Boolean = false,
)

// ---- Hotel content (fetched by hotel id after search; merged into results) ----------------------
// Content-by-region returns 204, so we look it up by the ids that came back from the search.

@Serializable
data class HotelContentRequestDto(
    val channelId: String,
    val culture: String,
    val hotelIds: List<String>,
    val contentFields: List<String>,
)

@Serializable
data class HotelContentResponseDto(val hotels: List<HotelContentDto> = emptyList())

@Serializable
data class HotelContentDto(
    val id: String,
    val name: String? = null,
    val category: String? = null,
    val type: String? = null,
    val starRating: String? = null,
    val heroImage: String? = null,
    val distance: String? = null,
    val contact: ContentContactDto? = null,
    val attributes: List<ContentAttributeDto>? = null,
    // Raw so an unexpected facilities shape can't break the whole content parse; names extracted best-effort.
    val facilities: JsonElement? = null,
)

@Serializable
data class ContentAttributeDto(val key: String? = null, val value: String? = null)

@Serializable
data class ContentContactDto(val address: ContentAddressDto? = null)

@Serializable
data class ContentAddressDto(val line1: String? = null, val city: ContentCityDto? = null)

@Serializable
data class ContentCityDto(val name: String? = null)

/**
 * Maps a raw search result (rate/options) merged with optional [content] (name/image/rating/address)
 * to the domain hotel. Content is best-effort — null fields render with placeholders in the UI.
 */
fun HotelResultDto.toWholesaleHotel(
    currency: String,
    nights: Int,
    content: HotelContentDto? = null,
): WholesaleHotel {
    val total = (rate?.totalRate ?: 0.0).roundToInt()
    val perNight = if (nights > 0) total / nights else total
    return WholesaleHotel(
        id = id,
        name = content?.name,
        imageUrl = content?.heroImage,
        rating = content?.starRating?.toDoubleOrNull(),
        reviewsCount = null,
        category = content?.category ?: rate?.boardBasis?.type,
        address = content?.contact?.address?.city?.name ?: content?.contact?.address?.line1,
        totalRate = total,
        perNightRate = perNight,
        currency = currency,
        boardBasis = rate?.boardBasis?.description?.trim()?.ifBlank { null },
        refundable = options?.refundable ?: (rate?.refundability?.equals("Refundable", ignoreCase = true) ?: false),
        freeCancellation = options?.freeCancellation ?: false,
        freeBreakfast = options?.freeBreakfast ?: false,
        payAtHotel = options?.payAtHotel ?: false,
        offerText = rate?.offer?.description?.trim()?.ifBlank { null },
        facilities = content.facilityNames(),
    )
}

/**
 * Best-effort facility labels for the result card. Prefers the `facilities` content field (parsed
 * leniently from raw JSON so an unexpected shape can't break the merge); falls back to Basic
 * `themes_*` attributes (e.g. "Luxury property" → "Luxury").
 */
private fun HotelContentDto?.facilityNames(): List<String> {
    if (this == null) return emptyList()
    val fromFacilities = (facilities as? JsonArray).orEmpty().mapNotNull { element ->
        when (element) {
            is JsonPrimitive -> element.contentOrNull
            is JsonObject -> (element["name"] as? JsonPrimitive)?.contentOrNull
            else -> null
        }
    }.filter { it.isNotBlank() }
    if (fromFacilities.isNotEmpty()) return fromFacilities.distinct().take(6)

    return attributes.orEmpty()
        .filter { it.key?.startsWith("themes_") == true }
        .mapNotNull { it.value?.removeSuffix(" property")?.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(6)
}
