package com.resthalflab.resthalfapp.feature.search.data

import com.resthalflab.resthalfapp.feature.search.data.dto.ListingSummaryDto
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay

// Phase 2 stub. Replace with:
//   client.get("listings") { parameter("destination", destination); parameter("guests", guests) }
//       .body<List<ListingSummaryDto>>()
class SearchRemote(
    @Suppress("unused") private val client: HttpClient,
) {
    suspend fun search(destination: String, guests: Int): List<ListingSummaryDto> {
        delay(500)
        val all = listOf(
            ListingSummaryDto("aria", "Amaris Hotel Thamrin", "Jakarta", 250_000, "IDR", 4.2, reviewsCount = 1_240),
            ListingSummaryDto("mawar", "Yello Hotel Harmoni", "Jakarta", 220_000, "IDR", 4.1, reviewsCount = 856),
            ListingSummaryDto("samudra", "favehotel LTC Glodok", "Jakarta", 180_000, "IDR", 3.9, reviewsCount = 643),
            ListingSummaryDto("rinjani", "Rinjani Lodge", "Lombok", 540_000, "IDR", 4.3, reviewsCount = 980),
            ListingSummaryDto("borobudur", "Borobudur View Inn", "Yogyakarta", 480_000, "IDR", 4.5, reviewsCount = 2_100),
        )
        if (destination.isBlank()) return all
        return all.filter { it.city.contains(destination, ignoreCase = true) || it.name.contains(destination, ignoreCase = true) }
    }
}
