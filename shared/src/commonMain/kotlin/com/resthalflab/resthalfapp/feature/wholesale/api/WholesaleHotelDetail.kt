package com.resthalflab.resthalfapp.feature.wholesale.api

/**
 * Full hotel detail: the "Hotel Details" half comes from getHotelContent (name/hero/rating/about/
 * highlights) and the "Room Details" half ([rooms]) from the roomsandrates call. [nextToken] is the
 * fresh token returned by roomsandrates, carried forward to the pricing/booking steps.
 */
data class WholesaleHotelDetail(
    val id: String,
    val name: String,
    val heroImage: String?,
    val starRating: Int?,
    val category: String?,
    val address: String?,
    val reviewRating: Double?,
    val reviewCount: Int?,
    val aboutText: String?,
    val highlights: List<String>,
    val allHighlights: List<String>,
    val reviewCategories: List<ReviewCategory>,
    val recommendPercent: Double?,
    val popularFacilities: List<String>,
    val locationAddress: String?,
    val geoLat: String?,
    val geoLong: String?,
    val nearbyAttractions: List<NearbyAttraction>,
    val rules: AccommodationRules?,
    val rooms: List<RoomOffer>,
    val currency: String,
    val nextToken: String?,
)

/** One category score out of 5 (e.g. "Cleanliness" → 4.4) for the review breakdown bars. */
data class ReviewCategory(val label: String, val rating: Double)

data class NearbyAttraction(val name: String, val distanceLabel: String?)

data class PolicyItem(val title: String, val text: String)

data class AccommodationRules(
    val checkInTime: String?,
    val checkOutTime: String?,
    val policies: List<PolicyItem>,
)

/** One standardized room (grouped) with its available rate options. */
data class RoomOffer(
    val standardRoomId: String,
    val name: String,
    val bedInfo: String?,
    val maxGuests: Int?,
    val facilities: List<String>,
    val images: List<String>,
    val options: List<RoomRateOption>,
)

/** A bookable price option for a room: board basis + refundability + price. */
data class RoomRateOption(
    val rateId: String,
    val recommendationId: String?,
    val totalRate: Int,
    val perNightRate: Int,
    val currency: String,
    val boardBasisLabel: String,
    val breakfastIncluded: Boolean,
    val refundable: Boolean,
)
