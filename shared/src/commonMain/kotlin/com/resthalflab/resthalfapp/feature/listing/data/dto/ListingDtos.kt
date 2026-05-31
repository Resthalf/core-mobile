package com.resthalflab.resthalfapp.feature.listing.data.dto

import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListingDetailDto(
    val id: String,
    val name: String,
    val city: String,
    val description: String,
    @SerialName("price_per_night") val pricePerNight: Int,
    @SerialName("service_fee") val serviceFee: Int = 0,
    val currency: String,
    val rating: Double,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("room_type") val roomType: String = "",
    val guests: Int = 1,
    @SerialName("bed_type") val bedType: String = "",
    @SerialName("area_sqm") val areaSqm: Int = 0,
    @SerialName("cancellation_policy") val cancellationPolicy: String = "",
    val amenities: List<String> = emptyList(),
    @SerialName("photo_urls") val photoUrls: List<String> = emptyList(),
)

fun ListingDetailDto.toDomain(): ListingDetail = ListingDetail(
    id = id,
    name = name,
    city = city,
    description = description,
    pricePerNight = pricePerNight,
    serviceFee = serviceFee,
    currency = currency,
    rating = rating,
    reviewsCount = reviewsCount,
    roomType = roomType,
    guests = guests,
    bedType = bedType,
    areaSqm = areaSqm,
    cancellationPolicy = cancellationPolicy,
    amenities = amenities,
    photoUrls = photoUrls,
)
