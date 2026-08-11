package com.resthalflab.resthalfapp.feature.staff.domain

import com.resthalflab.resthalfapp.feature.search.api.SlotType
import kotlin.time.Instant

/** Pure helpers for the staff dashboard (slot derivation from a stay window). */
object StaffTime {

    private fun parse(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

    /**
     * Infers the slot type from a window length, since the dashboard delegation payloads don't carry it.
     * A 12h stay → HALF_DAY, a 24h stay → FULL_DAY; we split at the 18h midpoint.
     */
    fun slotFromWindow(startIso: String, endIso: String): SlotType {
        val start = parse(startIso)
        val end = parse(endIso)
        if (start == null || end == null) return SlotType.HALF_DAY
        val hours = (end - start).inWholeMinutes / 60.0
        return if (hours < 18) SlotType.HALF_DAY else SlotType.FULL_DAY
    }
}
