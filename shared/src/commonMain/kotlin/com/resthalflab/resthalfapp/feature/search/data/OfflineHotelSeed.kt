package com.resthalflab.resthalfapp.feature.search.data

/**
 * Bundled, offline day-room inventory. Serves search while the RestHalf backend is retired and before
 * the Zentrumhub Nexus search spec lands. Curated entries add flavour for well-known cities; any other
 * city gets deterministic generated hotels so search always returns something offline.
 */
object OfflineHotelSeed {

    data class SeedRoom(val number: String, val halfPrice: Int, val fullPrice: Int)
    data class SeedHotel(val id: String, val name: String, val badge: String?, val rooms: List<SeedRoom>)

    fun hotelsFor(city: String): List<SeedHotel> {
        val key = city.trim().lowercase()
        return curated[key] ?: generate(city)
    }

    private val curated: Map<String, List<SeedHotel>> = mapOf(
        "jakarta" to listOf(
            SeedHotel("jkt-01", "Menteng Stay Inn", "Popular", listOf(
                SeedRoom("201", 165_000, 285_000),
                SeedRoom("305", 185_000, 320_000),
            )),
            SeedHotel("jkt-02", "Sudirman Rest Suites", null, listOf(
                SeedRoom("410", 210_000, 360_000),
                SeedRoom("512", 240_000, 410_000),
            )),
            SeedHotel("jkt-03", "Kemang Nap Hotel", "Great price", listOf(
                SeedRoom("102", 140_000, 250_000),
            )),
        ),
        "bandung" to listOf(
            SeedHotel("bdg-01", "Dago Hillside Rooms", "Popular", listOf(
                SeedRoom("11", 135_000, 240_000),
                SeedRoom("14", 155_000, 270_000),
            )),
            SeedHotel("bdg-02", "Riau Street Rest House", null, listOf(
                SeedRoom("22", 160_000, 285_000),
            )),
        ),
        "surabaya" to listOf(
            SeedHotel("sby-01", "Tunjungan Day Hotel", "Popular", listOf(
                SeedRoom("301", 150_000, 260_000),
                SeedRoom("308", 170_000, 300_000),
            )),
            SeedHotel("sby-02", "Gubeng Transit Suites", null, listOf(
                SeedRoom("77", 190_000, 330_000),
            )),
        ),
        "yogyakarta" to listOf(
            SeedHotel("ygy-01", "Malioboro Nap House", "Popular", listOf(
                SeedRoom("5", 120_000, 210_000),
                SeedRoom("9", 140_000, 235_000),
            )),
            SeedHotel("ygy-02", "Prawirotaman Rest Inn", "Great price", listOf(
                SeedRoom("31", 110_000, 195_000),
            )),
        ),
        "bali" to listOf(
            SeedHotel("bli-01", "Seminyak Daybreak Rooms", "Popular", listOf(
                SeedRoom("A2", 220_000, 380_000),
                SeedRoom("B4", 260_000, 440_000),
            )),
            SeedHotel("bli-02", "Kuta Layover Lodge", null, listOf(
                SeedRoom("12", 180_000, 310_000),
            )),
        ),
    )

    private fun generate(city: String): List<SeedHotel> {
        val display = city.trim().ifBlank { "City" }
        val base = 130_000 + (display.lowercase().hashCode().mod(9)) * 10_000
        val slug = display.lowercase().filter { it.isLetterOrDigit() }.take(6).ifBlank { "city" }
        return listOf("Central", "Grand", "Cozy", "Airport").mapIndexed { i, prefix ->
            val price = base + i * 25_000
            SeedHotel(
                id = "gen-$slug-$i",
                name = "$prefix $display Rest Hotel",
                badge = if (i == 0) "Popular" else null,
                rooms = listOf(
                    SeedRoom("${i + 1}01", price, price + 120_000),
                    SeedRoom("${i + 1}05", price + 30_000, price + 160_000),
                ),
            )
        }
    }
}
