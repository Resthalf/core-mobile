package com.resthalflab.resthalfapp.feature.wholesale

import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.search.ui.home.filterForSlot
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationType
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.AutosuggestResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.toDomain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class AutosuggestMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Trimmed shape of the real Zentrumhub autosuggest response: a City, an Airport (with code),
    // a Hotel (with referenceId/city/state), and an unrecognized type.
    private val sample = """
        {
          "locationSuggestions": [
            {"id":"227746","name":"Yogyakarta","fullName":"Yogyakarta, Special Region of Yogyakarta, Indonesia","type":"City","country":"ID","coordinates":{"lat":-7.795105,"long":110.365547},"referenceScore":150000},
            {"id":"325402","name":"Yogyakarta (YIA)","code":"yia","fullName":"Yogyakarta, Indonesia (YIA)","type":"Airport","country":"ID","coordinates":{"lat":-7.897944,"long":110.059779}},
            {"id":"9917481454","name":"Hotel Purnama","fullName":"Hotel Purnama, Yogyakarta, ID","type":"Hotel","city":"Yogyakarta","state":"DIY","country":"ID","coordinates":{"lat":-7.7551,"long":110.3854},"referenceId":"17481454"},
            {"id":"1","name":"Somewhere","type":"Village","country":"ID"}
          ],
          "status": "success"
        }
    """.trimIndent()

    private fun parsed() = json.decodeFromString<AutosuggestResponseDto>(sample)
        .locationSuggestions.map { it.toDomain() }

    @Test
    fun maps_dto_fields_and_types() {
        val domain = parsed()
        domain shouldHaveSize 4

        val city = domain[0]
        city.type shouldBe LocationType.CITY
        city.coordinates.long shouldBe 110.365547 // @SerialName("long") wired correctly
        city.referenceScore shouldBe 150000L

        val airport = domain[1]
        airport.type shouldBe LocationType.AIRPORT
        airport.code shouldBe "yia"

        val hotel = domain[2]
        hotel.type shouldBe LocationType.HOTEL
        hotel.city shouldBe "Yogyakarta"
        hotel.referenceId shouldBe "17481454"

        val unknown = domain[3]
        unknown.type shouldBe LocationType.UNKNOWN
        unknown.fullName shouldBe "Somewhere"          // missing fullName falls back to name
        unknown.coordinates.lat shouldBe 0.0            // missing coordinates default to (0,0)
    }

    @Test
    fun half_day_keeps_only_cities_full_day_keeps_all() {
        val all = parsed()
        all.filterForSlot(SlotType.HALF_DAY).map { it.type } shouldBe listOf(LocationType.CITY)
        all.filterForSlot(SlotType.FULL_DAY) shouldHaveSize 4
    }
}
