package com.resthalflab.resthalfapp.feature.search.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.search.data.dto.RoomOfferDto
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.formatClock12h
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult
import com.resthalflab.resthalfapp.feature.search.domain.model.RoomOption
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class DefaultSearchRepository(
    private val remote: SearchRemote,
) : SearchRepository {

    override suspend fun search(args: SearchArgs): AppResult<List<HotelSearchResult>> = safeApiCall {
        val response = remote.search(
            city = args.city,
            date = args.date,
            nights = args.nights,
            adults = args.adults,
            includeWholesale = false,
        )
        response.direct
            .filter { it.slotType.toSlotTypeOrNull() == args.slotType }
            .groupBy { it.hotel.id }
            .map { (_, offers) -> offers.toHotelResult(args.slotType) }
            .sortedBy { it.fromPrice }
    }

    private fun List<RoomOfferDto>.toHotelResult(slotType: SlotType): HotelSearchResult {
        val first = first()
        val rooms = distinctBy { it.roomId }
            .map {
                RoomOption(
                    roomId = it.roomId,
                    roomNumber = it.roomNumber ?: "—",
                    slotType = slotType,
                    price = it.price,
                    currency = it.currency,
                )
            }
            .sortedBy { it.price }
        return HotelSearchResult(
            hotelId = first.hotel.id,
            hotelName = first.hotel.name,
            city = first.hotel.city,
            slotType = slotType,
            slotLabel = first.label ?: slotType.name,
            badge = first.badge,
            windowLabel = windowLabel(first.startTime, first.endTime),
            fromPrice = rooms.minOfOrNull { it.price } ?: first.price,
            currency = first.currency,
            roomCount = rooms.size,
            rooms = rooms,
        )
    }

    private fun windowLabel(startIso: String, endIso: String): String = runCatching {
        val tz = TimeZone.currentSystemDefault()
        val start = Instant.parse(startIso).toLocalDateTime(tz)
        val end = Instant.parse(endIso).toLocalDateTime(tz)
        "${formatClock12h(start)} – ${formatClock12h(end)}"
    }.getOrDefault("")

    private fun String.toSlotTypeOrNull(): SlotType? =
        SlotType.entries.firstOrNull { it.name == this }
}
