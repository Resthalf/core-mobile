package com.resthalflab.resthalfapp.feature.search

import com.resthalflab.resthalfapp.feature.search.ui.results.HotelFilters
import com.resthalflab.resthalfapp.feature.search.ui.results.PaymentFilter
import com.resthalflab.resthalfapp.feature.search.ui.results.SortOption
import com.resthalflab.resthalfapp.feature.search.ui.results.applyFilters
import com.resthalflab.resthalfapp.feature.search.ui.results.applySort
import com.resthalflab.resthalfapp.feature.search.ui.results.buildFacets
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResultsFiltersTest {

    private fun hotel(
        id: String,
        price: Int,
        rating: Double? = null,
        facilities: List<String> = emptyList(),
        freeCancellation: Boolean = false,
        freeBreakfast: Boolean = false,
        refundable: Boolean = false,
        payAtHotel: Boolean = false,
        offerText: String? = null,
    ) = WholesaleHotel(
        id = id,
        name = "Hotel $id",
        imageUrl = null,
        rating = rating,
        reviewsCount = null,
        category = "Hotel",
        address = null,
        totalRate = price,
        perNightRate = price,
        currency = "IDR",
        boardBasis = null,
        refundable = refundable,
        freeCancellation = freeCancellation,
        freeBreakfast = freeBreakfast,
        payAtHotel = payAtHotel,
        offerText = offerText,
        facilities = facilities,
    )

    private val hotels = listOf(
        hotel("a", price = 100, rating = 5.0, facilities = listOf("Pool", "WiFi"), freeCancellation = true, offerText = "-50%"),
        hotel("b", price = 200, rating = 4.0, facilities = listOf("WiFi"), freeBreakfast = true, payAtHotel = true),
        hotel("c", price = 300, rating = 3.0, facilities = listOf("Pool"), refundable = true),
        hotel("d", price = 400, rating = null),
    )

    @Test
    fun no_filters_keeps_everything() {
        hotels.applyFilters(HotelFilters()).map { it.id } shouldBe listOf("a", "b", "c", "d")
    }

    @Test
    fun price_range_is_inclusive() {
        hotels.applyFilters(HotelFilters(minPrice = 200, maxPrice = 300)).map { it.id } shouldBe listOf("b", "c")
    }

    @Test
    fun star_bucket_matches_floor_and_all_selected() {
        hotels.applyFilters(HotelFilters(stars = setOf(5, 4))).map { it.id } shouldBe listOf("a", "b")
    }

    @Test
    fun unrated_hotel_is_excluded_when_star_filter_active() {
        hotels.applyFilters(HotelFilters(stars = setOf(3))).map { it.id } shouldBe listOf("c")
    }

    @Test
    fun facilities_are_anded_and_case_insensitive() {
        hotels.applyFilters(HotelFilters(facilities = setOf("pool", "wifi"))).map { it.id } shouldBe listOf("a")
    }

    @Test
    fun boolean_and_deal_toggles_filter() {
        hotels.applyFilters(HotelFilters(freeCancellation = true)).map { it.id } shouldBe listOf("a")
        hotels.applyFilters(HotelFilters(deals = true)).map { it.id } shouldBe listOf("a")
    }

    @Test
    fun payment_filter_splits_on_pay_at_hotel() {
        hotels.applyFilters(HotelFilters(payment = PaymentFilter.PayAtHotel)).map { it.id } shouldBe listOf("b")
        hotels.applyFilters(HotelFilters(payment = PaymentFilter.PayNow)).map { it.id } shouldBe listOf("a", "c", "d")
    }

    @Test
    fun sorting_orders_by_price_and_rating() {
        hotels.applySort(SortOption.LowestPrice).map { it.id } shouldBe listOf("a", "b", "c", "d")
        hotels.applySort(SortOption.HighestPrice).map { it.id } shouldBe listOf("d", "c", "b", "a")
        hotels.applySort(SortOption.HighestRating).map { it.id } shouldBe listOf("a", "b", "c", "d")
        hotels.applySort(SortOption.Recommended).map { it.id } shouldBe listOf("a", "b", "c", "d")
    }

    @Test
    fun facets_expose_bounds_stars_and_facilities() {
        val facets = buildFacets(hotels)
        facets.priceMin shouldBe 100
        facets.priceMax shouldBe 400
        facets.stars shouldBe listOf(5, 4, 3)
        // WiFi and Pool both appear twice → ordered by frequency then name.
        facets.facilities shouldBe listOf("Pool", "WiFi")
    }

    @Test
    fun active_count_ignores_full_price_range() {
        val facets = buildFacets(hotels)
        HotelFilters().activeCount(facets) shouldBe 0
        HotelFilters(minPrice = 100, maxPrice = 400).activeCount(facets) shouldBe 0
        HotelFilters(minPrice = 150, stars = setOf(5), freeBreakfast = true).activeCount(facets) shouldBe 3
    }
}
