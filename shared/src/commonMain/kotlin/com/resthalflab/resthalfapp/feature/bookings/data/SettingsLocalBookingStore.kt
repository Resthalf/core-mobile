package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.feature.bookings.api.LocalBookingStore
import com.resthalflab.resthalfapp.feature.bookings.api.StoredBooking
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** [LocalBookingStore] backed by a JSON list in [Settings]. */
class SettingsLocalBookingStore(
    private val settings: Settings,
) : LocalBookingStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val _bookings = MutableStateFlow(load())
    override val bookings: StateFlow<List<StoredBooking>> = _bookings.asStateFlow()

    override fun get(id: String): StoredBooking? = _bookings.value.firstOrNull { it.id == id }

    override fun upsert(booking: StoredBooking) {
        persist(_bookings.value.filterNot { it.id == booking.id } + booking)
    }

    override fun markPaid(id: String) = update(id) { it.copy(paid = true) }

    override fun cancel(id: String) = update(id) { it.copy(cancelled = true) }

    override fun reschedule(id: String, startTime: String, endTime: String) =
        update(id) { it.copy(startTime = startTime, endTime = endTime) }

    override fun vacate(id: String, endedAt: String) =
        update(id) { it.copy(endedEarly = true, endedAt = endedAt) }

    private fun update(id: String, transform: (StoredBooking) -> StoredBooking) {
        persist(_bookings.value.map { if (it.id == id) transform(it) else it })
    }

    private fun persist(list: List<StoredBooking>) {
        _bookings.value = list
        settings.putString(KEY, json.encodeToString(list))
    }

    private fun load(): List<StoredBooking> =
        settings.getStringOrNull(KEY)
            ?.let { runCatching { json.decodeFromString<List<StoredBooking>>(it) }.getOrNull() }
            ?: emptyList()

    private companion object {
        const val KEY = "bookings.local"
    }
}
