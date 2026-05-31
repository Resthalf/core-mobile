package com.resthalflab.resthalfapp.feature.listing.api

data class ListingDetail(
    val id: String,
    val name: String,
    val city: String,
    val description: String,
    val pricePerNight: Int,
    val serviceFee: Int,
    val currency: String,
    val rating: Double,
    val reviewsCount: Int,
    val roomType: String,
    val guests: Int,
    val bedType: String,
    val areaSqm: Int,
    val cancellationPolicy: String,
    val amenities: List<String>,
    val photoUrls: List<String>,
) {
    val totalPrice: Int get() = pricePerNight + serviceFee
}
