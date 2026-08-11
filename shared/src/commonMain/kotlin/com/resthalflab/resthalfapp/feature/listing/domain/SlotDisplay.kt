package com.resthalflab.resthalfapp.feature.listing.domain

/** Display strings for the raw slotType ("HALF_DAY" / "FULL_DAY"). */
object SlotDisplay {
    fun title(slot: String): String = if (slot == "FULL_DAY") "Full Day Stay" else "Night Stay"

    fun windowLine(slot: String): String =
        if (slot == "FULL_DAY") "12:00 PM – 12:00 PM next day (24 hours)" else "12:00 AM – 12:00 PM (12 hours)"

    fun checkInOutLine(slot: String): String =
        if (slot == "FULL_DAY") "Check-in at 12:00 PM, check-out by 12:00 PM next day"
        else "Check-in at 12:00 AM, check-out by 12:00 PM"

    fun windowShort(slot: String): String = if (slot == "FULL_DAY") "12PM–12PM" else "12AM–12PM"
}
