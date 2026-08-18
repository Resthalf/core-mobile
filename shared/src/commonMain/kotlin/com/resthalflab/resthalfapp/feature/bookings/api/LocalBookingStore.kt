package com.resthalflab.resthalfapp.feature.bookings.api

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * A booking persisted on-device. Raw fields only (times as ISO strings); display formatting +
 * status are derived when mapping to the bookings domain model. This is the single source of truth
 * for the offline guest booking loop (created by listing, read/mutated by the bookings feature).
 */
@Serializable
data class StoredBooking(
    val id: String,
    val bookingCode: String,
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val roomId: String,
    val roomNumber: String,
    val slotType: String,
    val totalPrice: Int,
    val currency: String,
    val startTime: String,
    val endTime: String,
    val paid: Boolean = false,
    val cancelled: Boolean = false,
    val endedEarly: Boolean = false,
    val endedAt: String? = null,
    val createdAt: Long = 0L,
)

/** Device-persisted bookings. Public so the listing feature can create bookings into it. */
interface LocalBookingStore {
    val bookings: StateFlow<List<StoredBooking>>
    fun get(id: String): StoredBooking?
    fun upsert(booking: StoredBooking)
    fun markPaid(id: String)
    fun cancel(id: String)
    fun reschedule(id: String, startTime: String, endTime: String)
    fun vacate(id: String, endedAt: String)
}
