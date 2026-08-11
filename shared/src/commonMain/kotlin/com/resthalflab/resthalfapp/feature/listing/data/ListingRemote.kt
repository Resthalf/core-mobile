package com.resthalflab.resthalfapp.feature.listing.data

import com.resthalflab.resthalfapp.feature.listing.data.dto.ListingDetailDto
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay

// Phase 2 stub. Replace with:  client.get("listings/$id").body<ListingDetailDto>()
class ListingRemote(
    @Suppress("unused") private val client: HttpClient,
) {
    suspend fun getListing(id: String): ListingDetailDto {
        delay(400)
        val (name, city, price, rating, reviews) = catalog[id] ?: Fallback(id)
        return ListingDetailDto(
            id = id,
            name = name,
            city = city,
            description = "A comfortable day-use room in the heart of the city — perfect for rest, " +
                "work, or a layover between flights. Check in at midnight, check out by noon.",
            pricePerNight = price,
            serviceFee = 10_000,
            currency = "IDR",
            rating = rating,
            reviewsCount = reviews,
            roomType = "Smart Room Only",
            guests = 2,
            bedType = "Queen Bed",
            areaSqm = 16,
            cancellationPolicy = "Free cancellation before 11:59 PM, 23 May 2024",
            amenities = listOf("Free WiFi", "Air conditioning", "Breakfast", "Parking"),
            photoUrls = (1..8).map { "https://picsum.photos/seed/${id}_$it/900/600" },
        )
    }

    private data class Entry(
        val name: String,
        val city: String,
        val price: Int,
        val rating: Double,
        val reviews: Int,
    )

    @Suppress("FunctionName")
    private fun Fallback(id: String) = Entry("Hotel ${id.uppercase()}", "Jakarta", 250_000, 4.2, 1_240)

    private val catalog = mapOf(
        "aria" to Entry("Amaris Hotel Thamrin", "Jakarta", 250_000, 4.2, 1_240),
        "mawar" to Entry("Yello Hotel Harmoni", "Jakarta", 220_000, 4.1, 856),
        "samudra" to Entry("favehotel LTC Glodok", "Jakarta", 180_000, 3.9, 643),
        "rinjani" to Entry("Rinjani Lodge", "Lombok", 540_000, 4.3, 980),
        "borobudur" to Entry("Borobudur View Inn", "Yogyakarta", 480_000, 4.5, 2_100),
    )
}
