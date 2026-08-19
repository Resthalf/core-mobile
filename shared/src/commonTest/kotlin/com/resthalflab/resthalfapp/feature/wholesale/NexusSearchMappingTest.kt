package com.resthalflab.resthalfapp.feature.wholesale

import com.resthalflab.resthalfapp.feature.wholesale.data.dto.SearchResultsDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.toWholesaleHotel
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class NexusSearchMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Trimmed shape of a real Nexus availability/results batch: one refundable rate with an offer,
    // one non-refundable rate without one.
    private val sample = """
        {
          "status": "InProgress",
          "currency": "IDR",
          "hotels": [
            {
              "id": "39391041",
              "rate": {
                "totalRate": 2524499.00,
                "baseRate": 2086363.00,
                "taxes": 438136.00,
                "boardBasis": {"description": "Sarapan Prasmanan", "type": "BedAndBreakfast"},
                "refundability": "Refundable",
                "offer": {"title": "Promotion", "description": "Sale tertutup: hemat 10% "}
              },
              "options": {"freeBreakfast": true, "freeCancellation": true, "refundable": true, "payAtHotel": true}
            },
            {
              "id": "39606769",
              "rate": {
                "totalRate": 13063763.00,
                "boardBasis": {"description": "Sarapan gratis", "type": "BedAndBreakfast"},
                "refundability": "NonRefundable"
              },
              "options": {"freeBreakfast": true, "freeCancellation": false, "refundable": false, "payAtHotel": false}
            }
          ]
        }
    """.trimIndent()

    @Test
    fun maps_rate_and_options() {
        val hotels = json.decodeFromString<SearchResultsDto>(sample)
            .hotels.map { it.toWholesaleHotel(currency = "IDR", nights = 1) }

        hotels shouldHaveSize 2

        val a = hotels[0]
        a.id shouldBe "39391041"
        a.totalRate shouldBe 2_524_499
        a.perNightRate shouldBe 2_524_499
        a.currency shouldBe "IDR"
        a.refundable shouldBe true
        a.freeBreakfast shouldBe true
        a.freeCancellation shouldBe true
        a.payAtHotel shouldBe true
        a.boardBasis shouldBe "Sarapan Prasmanan"
        a.offerText shouldBe "Sale tertutup: hemat 10%"

        val b = hotels[1]
        b.id shouldBe "39606769"
        b.totalRate shouldBe 13_063_763
        b.refundable shouldBe false
        b.offerText shouldBe null
    }

    @Test
    fun per_night_divides_total_by_nights() {
        val hotel = json.decodeFromString<SearchResultsDto>(sample)
            .hotels.first().toWholesaleHotel(currency = "IDR", nights = 2)
        hotel.perNightRate shouldBe 2_524_499 / 2
    }
}
