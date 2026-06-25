package com.resthalflab.resthalfapp.feature.bookings.domain.model

/** Result of the cancellation eligibility check (GET /bookings/{id}/cancel-preview). */
data class CancelPreview(
    val allowed: Boolean,
    val policyType: String,
    val originalAmount: Int,
    val refundAmount: Int,
    val refundPercent: Int,
    val deadlinePassed: Boolean,
    val reason: String,
)
