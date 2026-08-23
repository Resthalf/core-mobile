package com.resthalflab.resthalfapp.feature.search.api

import kotlinx.serialization.Serializable

/**
 * Opens the checkout flow for a chosen room rate option. Carries the roomsandrates [token] +
 * [recommendationId] (needed for the price-check and booking calls) plus the summary fields the
 * checkout shows. [totalRate] is what the user last saw — compared against the fresh price-check.
 */
@Serializable
data class CheckoutArgs(
    val hotelId: String,
    val token: String,
    val recommendationId: String,
    val rateId: String,
    val hotelName: String,
    val heroImage: String? = null,
    val location: String? = null,
    val roomName: String,
    val boardBasisLabel: String,
    val breakfastIncluded: Boolean,
    val refundable: Boolean,
    val checkIn: String,
    val checkOut: String,
    val nights: Int,
    val totalRate: Int,
    val perNightRate: Int,
    val currency: String,
)
