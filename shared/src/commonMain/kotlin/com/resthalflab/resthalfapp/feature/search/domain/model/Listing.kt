package com.resthalflab.resthalfapp.feature.search.domain.model

data class Listing(
    val id: String,
    val name: String,
    val city: String,
    val pricePerNight: Int,
    val currency: String,
    val rating: Double,
    val reviewsCount: Int,
    val thumbnailUrl: String?,
)

data class SearchQuery(
    val destination: String = "",
    val guests: Int = 2,
)
