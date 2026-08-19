package com.resthalflab.resthalfapp.feature.wholesale.api

import kotlinx.serialization.Serializable

/**
 * One hotel in the wholesale (Zentrumhub) results list. Rate + options come from the availability
 * search; [name]/[imageUrl]/[rating]/[reviewsCount]/[category] come from the separate hotel-content
 * API and are null until that call is wired.
 */
@Serializable
data class WholesaleHotel(
    val id: String,
    val name: String?,
    val imageUrl: String?,
    val rating: Double?,
    val reviewsCount: Int?,
    val category: String?,
    val address: String?,
    val totalRate: Int,
    val perNightRate: Int,
    val currency: String,
    val boardBasis: String?,
    val refundable: Boolean,
    val freeCancellation: Boolean,
    val freeBreakfast: Boolean,
    val payAtHotel: Boolean,
    val offerText: String?,
    val facilities: List<String> = emptyList(),
)
