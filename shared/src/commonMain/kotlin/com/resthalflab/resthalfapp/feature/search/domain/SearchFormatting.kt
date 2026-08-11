package com.resthalflab.resthalfapp.feature.search.domain

import com.resthalflab.resthalfapp.feature.search.api.SlotType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** "Fri, 24 May 2024" */
fun formatLongDate(date: LocalDate): String {
    val dow = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$dow, ${date.dayOfMonth} $month ${date.year}"
}

/** Parses ISO yyyy-MM-dd then formats; falls back to the raw string. */
fun formatLongDate(isoDate: String): String =
    runCatching { formatLongDate(LocalDate.parse(isoDate)) }.getOrDefault(isoDate)

/** "12:00 AM" */
fun formatClock12h(time: LocalDateTime): String {
    val hour12 = when (val h = time.hour % 12) {
        0 -> 12
        else -> h
    }
    val period = if (time.hour < 12) "AM" else "PM"
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour12:$minute $period"
}

// Word embedded in the CTA, e.g. "Search Night Rooms".
val SlotType.ctaWord: String get() = when (this) {
    SlotType.HALF_DAY -> "Night"
    SlotType.FULL_DAY -> "Full Day"
}

val SlotType.stayTitle: String get() = when (this) {
    SlotType.HALF_DAY -> "Night Stay"
    SlotType.FULL_DAY -> "Full Day Stay"
}

// Full window line for the stay-window banner.
val SlotType.windowLong: String get() = when (this) {
    SlotType.HALF_DAY -> "12:00 AM – 12:00 PM (12 hours)"
    SlotType.FULL_DAY -> "12:00 PM – 12:00 PM (24 hours)"
}

// Compact window for chips/subtitles.
val SlotType.windowShort: String get() = when (this) {
    SlotType.HALF_DAY -> "12AM–12PM"
    SlotType.FULL_DAY -> "12PM–12PM"
}

fun SlotType.searchButtonText(): String = "Search $ctaWord Rooms"
