package com.resthalflab.resthalfapp.feature.listing.api

import kotlinx.coroutines.flow.StateFlow

interface ListingDetailComponent {
    val state: StateFlow<UiState>
    fun onBackClicked()
    fun onBookClicked()

    data class UiState(
        val hotelName: String,
        val city: String,
        val roomNumber: String,
        val roomId: String,
        val stayTitle: String,
        val stayWindowLine: String,
        val checkInOutLine: String,
        val photoUrls: List<String>,
        val priceLabel: String,
        val bookButtonText: String,
        val submitting: Boolean = false,
        val error: String? = null,
    )
}
