package com.resthalflab.resthalfapp.feature.search.data.dto

import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListingSummaryDto(
    val id: String,
    val name: String,
    val city: String,
    @SerialName("price_per_night") val pricePerNight: Int,
    val currency: String,
    val rating: Double,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

fun ListingSummaryDto.toDomain(): Listing = Listing(
    id = id,
    name = name,
    city = city,
    pricePerNight = pricePerNight,
    currency = currency,
    rating = rating,
    reviewsCount = reviewsCount,
    thumbnailUrl = thumbnailUrl,
)
