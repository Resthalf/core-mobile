package com.resthalflab.resthalfapp.feature.bookings.domain.model

/** Outcome of releasing a room early. [message] is the backend's user-facing confirmation. */
data class VacateResult(
    val message: String,
    val earlyByMinutes: Int,
)
