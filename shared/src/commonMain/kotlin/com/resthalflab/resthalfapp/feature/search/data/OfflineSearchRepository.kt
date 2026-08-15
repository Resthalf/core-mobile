package com.resthalflab.resthalfapp.feature.search.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.formatClock12h
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult
import com.resthalflab.resthalfapp.feature.search.domain.model.RoomOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Offline day-room search backed by [OfflineHotelSeed]. Filters the bundled inventory by city and
 * builds stay windows from the searched date + slot. This is the interim source until the Zentrumhub
 * Nexus search spec lands; it also serves as the offline cache the future Nexus repo can populate.
 */
class OfflineSearchRepository : SearchRepository {

    override suspend fun search(args: SearchArgs): AppResult<List<HotelSearchResult>> =
        safeApiCall { withContext(Dispatchers.Default) { buildResults(args) } }

    private fun buildResults(args: SearchArgs): List<HotelSearchResult> {
        val (startIso, endIso) = slotWindow(args.date, args.slotType)
        val window = windowLabel(startIso, endIso)
        return OfflineHotelSeed.hotelsFor(args.city).map { seed ->
            val rooms = seed.rooms.map { room ->
                RoomOption(
                    roomId = "${seed.id}-${room.number}",
                    roomNumber = room.number,
                    slotType = args.slotType,
                    price = if (args.slotType == SlotType.HALF_DAY) room.halfPrice else room.fullPrice,
                    currency = CURRENCY,
                    startTime = startIso,
                    endTime = endIso,
                )
            }.sortedBy { it.price }
            HotelSearchResult(
                hotelId = seed.id,
                hotelName = seed.name,
                city = args.city.ifBlank { "—" },
                slotType = args.slotType,
                slotLabel = args.slotType.name,
                badge = seed.badge,
                windowLabel = window,
                fromPrice = rooms.minOf { it.price },
                currency = CURRENCY,
                roomCount = rooms.size,
                rooms = rooms,
            )
        }.sortedBy { it.fromPrice }
    }

    private fun slotWindow(isoDate: String, slot: SlotType): Pair<String, String> {
        val date = LocalDate.parse(isoDate)
        val tz = TimeZone.currentSystemDefault()
        return when (slot) {
            SlotType.HALF_DAY -> {
                val start = LocalDateTime(date, LocalTime(0, 0)).toInstant(tz)
                val end = LocalDateTime(date, LocalTime(12, 0)).toInstant(tz)
                start.toString() to end.toString()
            }
            SlotType.FULL_DAY -> {
                val start = LocalDateTime(date, LocalTime(12, 0)).toInstant(tz)
                val end = LocalDateTime(date.plus(DatePeriod(days = 1)), LocalTime(12, 0)).toInstant(tz)
                start.toString() to end.toString()
            }
        }
    }

    private fun windowLabel(startIso: String, endIso: String): String = runCatching {
        val tz = TimeZone.currentSystemDefault()
        val start = Instant.parse(startIso).toLocalDateTime(tz)
        val end = Instant.parse(endIso).toLocalDateTime(tz)
        "${formatClock12h(start)} – ${formatClock12h(end)}"
    }.getOrDefault("")

    private companion object {
        const val CURRENCY = "IDR"
    }
}
