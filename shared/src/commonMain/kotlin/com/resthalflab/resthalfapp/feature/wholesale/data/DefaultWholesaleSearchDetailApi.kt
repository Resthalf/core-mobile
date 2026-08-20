package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.ZentrumhubConfig
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.wholesale.api.AccommodationRules
import com.resthalflab.resthalfapp.feature.wholesale.api.NearbyAttraction
import com.resthalflab.resthalfapp.feature.wholesale.api.PolicyItem
import com.resthalflab.resthalfapp.feature.wholesale.api.ReviewCategory
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomOffer
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomRateOption
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleDetailApi
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotelDetail
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.ContentDescriptionDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.ContentRoomDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.HotelContentRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.RoomsAndRatesResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.StandardizedRoomDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.math.roundToInt

/**
 * Loads the hotel detail page: fires getHotelContent (best-effort — the page still renders rooms if
 * it fails) and roomsandrates in parallel, then maps the standardized room groups + their options
 * into the domain [WholesaleHotelDetail]. Board basis / refundability per option are read from the
 * standardized room's `mappedRoomRates` (matched by rate id).
 */
class DefaultWholesaleDetailApi(
    private val nexusRemote: NexusRemote,
    private val config: ZentrumhubConfig,
) : WholesaleDetailApi {

    override suspend fun loadDetail(
        hotelId: String,
        searchToken: String,
        checkIn: String,
        checkOut: String,
    ): AppResult<WholesaleHotelDetail> = safeApiCall {
        withContext(Dispatchers.Default) {
            coroutineScope {
                val nights = nightsBetween(checkIn, checkOut)
                val contentDeferred = async {
                    runCatching {
                        nexusRemote.getHotelContent(contentRequest(hotelId)).hotels.firstOrNull()
                    }.getOrNull()
                }
                val roomsAndRates = nexusRemote.roomsAndRates(hotelId, searchToken)
                val content = contentDeferred.await()
                buildDetail(hotelId, content, roomsAndRates, nights)
            }
        }
    }

    private fun buildDetail(
        hotelId: String,
        content: HotelContentDto?,
        roomsAndRates: RoomsAndRatesResponseDto,
        nights: Int,
    ): WholesaleHotelDetail {
        val currency = roomsAndRates.currency?.takeIf { it.isNotBlank() } ?: CURRENCY
        val groups = content?.facilityGroups.orEmpty()
        val hotelHighlights = groups.filter { it.type.equals("Hotel", ignoreCase = true) }
            .mapNotNull { it.name?.trim()?.ifBlank { null } }
            .distinct()
        val allHighlights = groups.mapNotNull { it.name?.trim()?.ifBlank { null } }.distinct()
        val review = content?.reviews?.firstOrNull()
        val categoryRatings = review?.categoryratings.orEmpty()
        val contentRoomsById = content?.rooms.orEmpty()
            .mapNotNull { room -> room.roomId?.let { it to room } }
            .toMap()

        return WholesaleHotelDetail(
            id = hotelId,
            name = content?.name?.ifBlank { null } ?: "Hotel $hotelId",
            heroImage = content?.heroImage,
            starRating = content?.starRating?.toIntOrNull(),
            category = content?.category ?: content?.type,
            address = content.addressLine(),
            reviewRating = review?.rating?.toDoubleOrNull(),
            reviewCount = review?.count?.toIntOrNull(),
            aboutText = content?.aboutText(),
            highlights = hotelHighlights.take(HIGHLIGHTS_PREVIEW),
            allHighlights = allHighlights,
            reviewCategories = categoryRatings
                .filter { !it.category.equals("recommendation_percent", ignoreCase = true) }
                .mapNotNull { cr ->
                    val label = cr.category?.let(::prettifyLabel) ?: return@mapNotNull null
                    val rating = cr.rating?.toDoubleOrNull() ?: return@mapNotNull null
                    ReviewCategory(label, rating)
                },
            recommendPercent = categoryRatings
                .firstOrNull { it.category.equals("recommendation_percent", ignoreCase = true) }
                ?.rating?.toDoubleOrNull(),
            popularFacilities = content?.facilities.parseFacilityNames(),
            locationAddress = content.fullAddress(),
            geoLat = content?.geoCode?.lat?.ifBlank { null },
            geoLong = content?.geoCode?.long?.ifBlank { null },
            nearbyAttractions = content?.nearByAttractions.orEmpty().mapNotNull { n ->
                val name = n.name?.trim()?.ifBlank { null } ?: return@mapNotNull null
                val distance = n.distance?.trim()?.ifBlank { null }
                    ?.let { d -> "$d ${n.unit.orEmpty()}".trim() }
                NearbyAttraction(name, distance)
            },
            rules = content.accommodationRules(),
            rooms = buildRooms(roomsAndRates, nights, currency, contentRoomsById),
            currency = currency,
            nextToken = roomsAndRates.token?.ifBlank { null },
        )
    }

    private fun buildRooms(
        roomsAndRates: RoomsAndRatesResponseDto,
        nights: Int,
        currency: String,
        contentRoomsById: Map<String, ContentRoomDto>,
    ): List<RoomOffer> {
        val hotel = roomsAndRates.hotel ?: return emptyList()
        val roomsById = hotel.standardizedRooms.associateBy { it.id }
        return hotel.standardizedRoomGroups.mapNotNull { group ->
            val stdRoomId = group.standardRoomIds.firstOrNull() ?: return@mapNotNull null
            val room = roomsById[stdRoomId] ?: return@mapNotNull null
            val options = group.options.mapNotNull { option ->
                val rateId = option.standardRooms.firstOrNull()?.rateIds?.firstOrNull()
                    ?: return@mapNotNull null
                val mapped = room.mappedRoomRates.firstOrNull { it.rateId == rateId }
                val total = option.totalRate.roundToInt()
                val board = mapped?.boardBasis
                RoomRateOption(
                    rateId = rateId,
                    recommendationId = option.recommendationId,
                    totalRate = total,
                    perNightRate = if (nights > 0) total / nights else total,
                    currency = currency,
                    boardBasisLabel = boardBasisLabel(board),
                    breakfastIncluded = board.includesBreakfast(),
                    refundable = mapped?.refundability.equals("Refundable", ignoreCase = true),
                )
            }.sortedBy { it.totalRate }
            if (options.isEmpty()) return@mapNotNull null
            RoomOffer(
                standardRoomId = stdRoomId,
                name = room.name?.ifBlank { null } ?: "Room",
                bedInfo = room.bedInfo?.ifBlank { null },
                maxGuests = room.maxGuestAllowed?.toIntOrNull(),
                facilities = room.facilities.mapNotNull { it.name?.trim()?.ifBlank { null } }
                    .distinct().take(ROOM_FACILITIES),
                imageUrl = room.heroImage() ?: contentRoomImage(room, contentRoomsById),
                options = options,
            )
        }.sortedBy { it.options.firstOrNull()?.totalRate ?: Int.MAX_VALUE }
    }

    private fun contentRequest(hotelId: String) = HotelContentRequestDto(
        channelId = config.channelId,
        culture = CONTENT_CULTURE,
        hotelIds = listOf(hotelId),
        contentFields = listOf("All"),
    )

    private fun nightsBetween(checkIn: String, checkOut: String): Int = runCatching {
        LocalDate.parse(checkIn).daysUntil(LocalDate.parse(checkOut)).coerceAtLeast(1)
    }.getOrDefault(1)

    private companion object {
        const val CURRENCY = "IDR"
        const val CONTENT_CULTURE = "en-US"
        const val HIGHLIGHTS_PREVIEW = 5
        const val ROOM_FACILITIES = 6
    }
}

// ---- Mapping helpers ----------------------------------------------------------------------------

private fun HotelContentDto?.addressLine(): String? {
    val address = this?.contact?.address ?: return null
    val city = address.city?.name?.trim()?.ifBlank { null }
    val line1 = address.line1?.trim()?.ifBlank { null }
    return listOfNotNull(line1, city).joinToString(", ").ifBlank { null }
}

/** Full postal address for the Location section. */
private fun HotelContentDto?.fullAddress(): String? {
    val address = this?.contact?.address ?: return null
    val countryLine = listOfNotNull(
        address.country?.name?.trim()?.ifBlank { null },
        address.postalCode?.trim()?.ifBlank { null },
    ).joinToString(" ").ifBlank { null }
    return listOfNotNull(
        address.line1?.trim()?.ifBlank { null },
        address.city?.name?.trim()?.ifBlank { null },
        address.state?.name?.trim()?.ifBlank { null },
        countryLine,
    ).joinToString(", ").ifBlank { null }
}

private fun HotelContentDto?.accommodationRules(): AccommodationRules? {
    if (this == null) return null
    val checkIn = checkinInfo?.let { info ->
        listOfNotNull(info.beginTime?.trim()?.ifBlank { null }, info.endTime?.trim()?.ifBlank { null })
            .joinToString(" – ").ifBlank { null }
    }
    val checkOut = checkoutInfo?.time?.trim()?.ifBlank { null }
    val policyItems = policies.orEmpty().mapNotNull { policy ->
        val text = policy.text?.stripHtml()?.ifBlank { null } ?: return@mapNotNull null
        PolicyItem(title = policy.type?.let(::prettifyLabel) ?: "Policy", text = text)
    }
    if (checkIn == null && checkOut == null && policyItems.isEmpty()) return null
    return AccommodationRules(checkInTime = checkIn, checkOutTime = checkOut, policies = policyItems)
}

private fun contentRoomImage(room: StandardizedRoomDto, byId: Map<String, ContentRoomDto>): String? {
    val codes = room.mappedRoomRates.mapNotNull { it.roomCode?.trim()?.ifBlank { null } }
    val match = codes.firstNotNullOfOrNull { byId[it] } ?: return null
    val links = match.image.firstOrNull()?.links ?: return null
    return (
        links.firstOrNull { it.size.equals("Xs", ignoreCase = true) }
            ?: links.firstOrNull { it.size.equals("Standard", ignoreCase = true) }
            ?: links.firstOrNull()
        )?.url
}

/** Parses the raw `facilities` array (objects with a `name`, or plain strings) into labels. */
private fun JsonElement?.parseFacilityNames(): List<String> {
    val array = this as? JsonArray ?: return emptyList()
    return array.mapNotNull { element ->
        when (element) {
            is JsonPrimitive -> element.contentOrNull
            is JsonObject -> (element["name"] as? JsonPrimitive)?.contentOrNull
            else -> null
        }
    }.map { it.trim() }.filter { it.isNotBlank() }.distinct()
}

/** "recommendation_percent" → "Recommendation Percent", "cleanliness" → "Cleanliness". */
private fun prettifyLabel(raw: String): String =
    raw.split('_', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

/** "About this property" text: the rooms description, HTML stripped. */
private fun HotelContentDto.aboutText(): String? {
    val rooms = descriptions?.firstOrNull { it.type.equals("rooms", ignoreCase = true) }
    val fallback = descriptions?.firstOrNull { it.type.equals("location", ignoreCase = true) }
    return (rooms ?: fallback).cleanText()
}

private fun ContentDescriptionDto?.cleanText(): String? =
    this?.text?.stripHtml()?.ifBlank { null }

private fun String.stripHtml(): String =
    replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun StandardizedRoomDto.heroImage(): String? {
    val links = images.firstOrNull()?.links ?: return null
    return (links.firstOrNull { it.size.equals("Standard", ignoreCase = true) } ?: links.firstOrNull())
        ?.url
}

private fun String?.includesBreakfast(): Boolean = when {
    this == null -> false
    equals("RoomOnly", ignoreCase = true) -> false
    equals("BedAndBreakfast", ignoreCase = true) -> true
    equals("HalfBoard", ignoreCase = true) -> true
    equals("FullBoard", ignoreCase = true) -> true
    equals("AllInclusive", ignoreCase = true) -> true
    else -> contains("breakfast", ignoreCase = true)
}

private fun boardBasisLabel(board: String?): String = when {
    board == null || board.isBlank() -> "Room only"
    board.equals("RoomOnly", ignoreCase = true) -> "Room only"
    board.equals("BedAndBreakfast", ignoreCase = true) -> "Breakfast included"
    board.equals("HalfBoard", ignoreCase = true) -> "Breakfast + dinner"
    board.equals("FullBoard", ignoreCase = true) -> "All meals included"
    board.equals("AllInclusive", ignoreCase = true) -> "All-inclusive"
    else -> board
}
