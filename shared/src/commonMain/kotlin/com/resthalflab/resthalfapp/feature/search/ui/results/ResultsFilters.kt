package com.resthalflab.resthalfapp.feature.search.ui.results

import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel

/**
 * Client-side filtering & sorting for the results list. Everything here runs over the hotels already
 * returned by the availability search — there is no extra Nexus call — so it's a pure, testable layer.
 *
 * The reference filter UI (tiket.com) has sections we can't back with data yet (guest-review score,
 * smoking, room facilities like kitchen/bathtub, hotel chains). Those are intentionally omitted; what
 * remains maps 1:1 onto [WholesaleHotel] fields.
 */

/** Sort options offered in the Sort sheet. "Highest star" from the reference == highest rating here
 * (our [WholesaleHotel.rating] is the hotel star class), so the two collapse into one. */
enum class SortOption(val label: String) {
    Recommended("Recommended"),
    HighestRating("Highest rating"),
    LowestPrice("Lowest price"),
    HighestPrice("Highest price"),
}

/** Which "when do I pay" bucket to keep. Backed by [WholesaleHotel.payAtHotel]. */
enum class PaymentFilter { Any, PayAtHotel, PayNow }

/** The committed filter selection. Empty means "show everything". */
data class HotelFilters(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val stars: Set<Int> = emptySet(),
    val facilities: Set<String> = emptySet(),
    val freeCancellation: Boolean = false,
    val freeBreakfast: Boolean = false,
    val refundable: Boolean = false,
    val deals: Boolean = false,
    val payment: PaymentFilter = PaymentFilter.Any,
) {
    /** True when the price range is narrower than the full range available in [facets]. */
    fun priceNarrowed(facets: FilterFacets): Boolean =
        (minPrice != null && minPrice > facets.priceMin) || (maxPrice != null && maxPrice < facets.priceMax)

    /** Number of active criteria — drives the count badge on the Filter chip. */
    fun activeCount(facets: FilterFacets?): Int {
        var n = 0
        if (facets != null && priceNarrowed(facets)) n++
        n += stars.size
        n += facilities.size
        if (freeCancellation) n++
        if (freeBreakfast) n++
        if (refundable) n++
        if (deals) n++
        if (payment != PaymentFilter.Any) n++
        return n
    }
}

/** The choices available to the filter UI, derived from the current result set. */
data class FilterFacets(
    val priceMin: Int,
    val priceMax: Int,
    val currency: String,
    /** Star buckets actually present, high → low (bucket 1 stands for "1 star or fewer"). */
    val stars: List<Int>,
    /** Distinct facilities across results, most common first. */
    val facilities: List<String>,
) {
    /** No meaningful choices to filter on (e.g. every hotel has the same single price and no facets). */
    val isEmpty: Boolean get() = priceMin >= priceMax && stars.isEmpty() && facilities.isEmpty()
}

/** Build the filter choices from the fetched hotels. */
fun buildFacets(hotels: List<WholesaleHotel>): FilterFacets {
    val prices = hotels.map { it.perNightRate }
    val stars = hotels.mapNotNull { it.rating?.toInt()?.coerceIn(1, 5) }
        .toSortedSet(compareByDescending { it })
        .toList()
    val facilities = hotels.flatMap { it.facilities }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { it.key }
    return FilterFacets(
        priceMin = prices.minOrNull() ?: 0,
        priceMax = prices.maxOrNull() ?: 0,
        currency = hotels.firstOrNull()?.currency ?: "",
        stars = stars,
        facilities = facilities,
    )
}

/** Keep only the hotels that satisfy every active criterion (criteria are ANDed together). */
fun List<WholesaleHotel>.applyFilters(f: HotelFilters): List<WholesaleHotel> = filter { h ->
    (f.minPrice == null || h.perNightRate >= f.minPrice) &&
        (f.maxPrice == null || h.perNightRate <= f.maxPrice) &&
        (f.stars.isEmpty() || starMatches(h.rating, f.stars)) &&
        (f.facilities.isEmpty() || f.facilities.all { wanted -> h.facilities.any { it.equals(wanted, ignoreCase = true) } }) &&
        (!f.freeCancellation || h.freeCancellation) &&
        (!f.freeBreakfast || h.freeBreakfast) &&
        (!f.refundable || h.refundable) &&
        (!f.deals || h.offerText != null) &&
        when (f.payment) {
            PaymentFilter.Any -> true
            PaymentFilter.PayAtHotel -> h.payAtHotel
            PaymentFilter.PayNow -> !h.payAtHotel
        }
}

/** Sort a (already filtered) list. [SortOption.Recommended] preserves the API's own order. */
fun List<WholesaleHotel>.applySort(sort: SortOption): List<WholesaleHotel> = when (sort) {
    SortOption.Recommended -> this
    SortOption.HighestRating -> sortedByDescending { it.rating ?: -1.0 }
    SortOption.LowestPrice -> sortedBy { it.perNightRate }
    SortOption.HighestPrice -> sortedByDescending { it.perNightRate }
}

/** A hotel matches a star selection when its star class equals a chosen bucket (bucket 1 == "≤1"). */
private fun starMatches(rating: Double?, stars: Set<Int>): Boolean {
    val s = rating?.toInt() ?: return false
    return stars.any { bucket -> if (bucket <= 1) s <= 1 else s == bucket }
}
