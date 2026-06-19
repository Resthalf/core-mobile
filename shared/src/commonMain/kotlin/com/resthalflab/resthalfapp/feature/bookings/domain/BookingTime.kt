package com.resthalflab.resthalfapp.feature.bookings.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Pure time helpers for the bookings feature (formatting + active countdown). */
object BookingTime {

    private val tz get() = TimeZone.currentSystemDefault()

    private fun parse(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

    /** "16 Jun 2026" */
    fun formatDate(iso: String): String {
        val d = parse(iso)?.toLocalDateTime(tz)?.date ?: return iso
        val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "${d.dayOfMonth} $month ${d.year}"
    }

    /** "2:40 AM" */
    fun formatClock12h(iso: String): String =
        parse(iso)?.toLocalDateTime(tz)?.let(::clock12) ?: ""

    /**
     * Local window for the booking list, e.g. "2:40 AM – 2:40 PM" (same day) or
     * "12:00 PM – 17 Jun, 12:00 PM" when the stay crosses midnight (full-day).
     */
    fun formatWindow(startIso: String, endIso: String): String {
        val start = parse(startIso)?.toLocalDateTime(tz) ?: return ""
        val end = parse(endIso)?.toLocalDateTime(tz) ?: return ""
        return if (start.date == end.date) {
            "${clock12(start)} – ${clock12(end)}"
        } else {
            "${clock12(start)} – ${dayMonth(end.date)}, ${clock12(end)}"
        }
    }

    private fun clock12(t: LocalDateTime): String {
        val hour12 = when (val h = t.hour % 12) { 0 -> 12; else -> h }
        val period = if (t.hour < 12) "AM" else "PM"
        return "$hour12:${t.minute.toString().padStart(2, '0')} $period"
    }

    private fun dayMonth(d: LocalDate): String {
        val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "${d.dayOfMonth} $month"
    }

    fun remainingSeconds(endIso: String, now: Instant = Clock.System.now()): Long {
        val end = parse(endIso) ?: return 0L
        return maxOf(0L, (end - now).inWholeSeconds)
    }

    /** hours, minutes, seconds remaining until [endIso]. */
    fun parts(endIso: String, now: Instant = Clock.System.now()): Triple<Int, Int, Int> {
        val s = remainingSeconds(endIso, now)
        return Triple((s / 3600).toInt(), ((s % 3600) / 60).toInt(), (s % 60).toInt())
    }

    /** 0f at [startIso] → 1f at [endIso]. */
    fun progress(startIso: String, endIso: String, now: Instant = Clock.System.now()): Float {
        val start = parse(startIso) ?: return 1f
        val end = parse(endIso) ?: return 1f
        val total = (end - start).inWholeSeconds.toFloat()
        if (total <= 0f) return 1f
        val elapsed = (now - start).inWholeSeconds.toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    /** Compact label for list rows: "5h 42m left", "12m left", or "Ended". */
    fun compactRemaining(endIso: String, now: Instant = Clock.System.now()): String {
        val s = remainingSeconds(endIso, now)
        if (s <= 0L) return "Ended"
        val h = s / 3600
        val m = (s % 3600) / 60
        return when {
            h > 0 -> "${h}h ${m}m left"
            m > 0 -> "${m}m left"
            else -> "<1m left"
        }
    }
}
