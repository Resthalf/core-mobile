package com.resthalflab.resthalfapp.core.storage

import com.russhwolf.settings.Settings

/**
 * Remembers bookings whose payment failed irrecoverably on the backend (e.g. "Slot data expired").
 * The backend keeps these as PENDING, so this client-side flag is what lets the UI mark them as
 * Internal Error and stop offering payment.
 *
 * MVP: a persisted id set. Graduate to a real backend FAILED/EXPIRED status when one exists, then delete this.
 */
interface FailedBookingStore {
    fun markFailed(bookingId: String)
    fun isFailed(bookingId: String): Boolean
}

class SettingsFailedBookingStore(
    private val settings: Settings,
) : FailedBookingStore {

    override fun markFailed(bookingId: String) {
        if (bookingId.isBlank()) return
        val ids = read()
        if (ids.add(bookingId)) settings.putString(KEY, ids.joinToString(SEP))
    }

    override fun isFailed(bookingId: String): Boolean = bookingId in read()

    private fun read(): MutableSet<String> =
        settings.getStringOrNull(KEY)
            ?.split(SEP)
            ?.filter { it.isNotBlank() }
            ?.toMutableSet()
            ?: mutableSetOf()

    private companion object {
        const val KEY = "bookings.failed"
        const val SEP = ","
    }
}
