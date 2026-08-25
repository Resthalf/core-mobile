package com.resthalflab.resthalfapp.feature.search.ui.results

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.resthalflab.resthalfapp.feature.search.api.HotelDetailArgs
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleHotel
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleSearchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

interface ResultsComponent {
    val state: StateFlow<UiState>

    /** Ids of hotels the user has favorited — drives the heart on each result. */
    val favoriteHotelIds: StateFlow<Set<String>>

    fun onToggleFavorite(hotel: WholesaleHotel)
    fun onViewHotel(hotel: WholesaleHotel)
    fun onBackClicked()
    fun onRetry()

    /** Apply a new filter selection (from the Filter sheet or a quick chip on the bar). */
    fun onApplyFilters(filters: HotelFilters)

    /** Change the sort order (from the Sort sheet). */
    fun onSortSelected(sort: SortOption)

    data class UiState(
        val locationLabel: String,
        val dateLabel: String,
        val guestsLabel: String,
        val loading: Boolean = false,
        /** All hotels returned by the search (unfiltered). */
        val results: List<WholesaleHotel> = emptyList(),
        /** [results] after filtering + sorting — what the list actually renders. */
        val visibleResults: List<WholesaleHotel> = emptyList(),
        /** Filter choices derived from [results]; null until the first search returns. */
        val facets: FilterFacets? = null,
        val filters: HotelFilters = HotelFilters(),
        val sort: SortOption = SortOption.Recommended,
        val error: String? = null,
    ) {
        val activeFilterCount: Int get() = filters.activeCount(facets)
    }
}

class DefaultResultsComponent(
    componentContext: ComponentContext,
    private val wholesaleSearch: WholesaleSearchApi,
    private val favorites: FavoritesRepository,
    private val args: SearchArgs,
    private val onBack: () -> Unit,
    private val onOpenDetail: (HotelDetailArgs) -> Unit,
) : ResultsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val checkIn = args.checkIn.ifBlank { args.date }
    private val checkOut = args.checkOut.ifBlank { args.date }

    // Search-session token needed to fetch rooms & rates for a chosen hotel.
    private var searchToken: String = ""

    private val _state = MutableStateFlow(
        ResultsComponent.UiState(
            locationLabel = args.location?.name ?: args.city.ifBlank { "Hotels" },
            dateLabel = dateRangeLabel(checkIn, checkOut),
            guestsLabel = guestsLabel(args.occupancy),
        )
    )
    override val state: StateFlow<ResultsComponent.UiState> = _state.asStateFlow()

    override val favoriteHotelIds: StateFlow<Set<String>> =
        favorites.favorites
            .map { list -> list.map(FavoriteHotel::hotelId).toSet() }
            .stateIn(scope, SharingStarted.Eagerly, favorites.favorites.value.map(FavoriteHotel::hotelId).toSet())

    init {
        search()
    }

    private fun search() {
        val location = args.location
        if (location == null) {
            _state.update {
                it.copy(loading = false, error = "Pick a destination from the suggestions to search hotels.")
            }
            return
        }
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = wholesaleSearch.searchHotels(location, checkIn, checkOut, args.occupancy)) {
                is AppResult.Success -> {
                    searchToken = result.value.token
                    val hotels = result.value.hotels
                    val facets = buildFacets(hotels)
                    _state.update {
                        it.copy(
                            loading = false,
                            results = hotels,
                            facets = facets,
                            visibleResults = hotels.applyFilters(it.filters).applySort(it.sort),
                        )
                    }
                }

                is AppResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
            }
        }
    }

    override fun onToggleFavorite(hotel: WholesaleHotel) {
        favorites.toggle(
            FavoriteHotel(
                hotelId = hotel.id,
                hotelName = hotel.name ?: "Hotel",
                city = hotel.address ?: _state.value.locationLabel,
                slotLabel = hotel.category ?: "Hotel",
                fromPrice = hotel.perNightRate,
                currency = hotel.currency,
            )
        )
    }

    override fun onViewHotel(hotel: WholesaleHotel) {
        if (searchToken.isBlank()) return
        onOpenDetail(
            HotelDetailArgs(
                hotelId = hotel.id,
                hotelName = hotel.name ?: "Hotel",
                heroImage = hotel.imageUrl,
                starRating = hotel.rating?.toInt(),
                reviewCount = hotel.reviewsCount,
                address = hotel.address,
                category = hotel.category,
                currency = hotel.currency,
                searchToken = searchToken,
                checkIn = checkIn,
                checkOut = checkOut,
            )
        )
    }

    override fun onApplyFilters(filters: HotelFilters) {
        _state.update { it.copy(filters = filters, visibleResults = it.results.applyFilters(filters).applySort(it.sort)) }
    }

    override fun onSortSelected(sort: SortOption) {
        _state.update { it.copy(sort = sort, visibleResults = it.results.applyFilters(it.filters).applySort(sort)) }
    }

    override fun onBackClicked() = onBack()
    override fun onRetry() = search()
}

private fun dateRangeLabel(checkIn: String, checkOut: String): String {
    val start = runCatching { LocalDate.parse(checkIn) }.getOrNull()
    val end = runCatching { LocalDate.parse(checkOut) }.getOrNull()
    if (start == null || end == null) return checkIn
    return "${dayMonth(start)} – ${dayMonth(end)}"
}

private fun dayMonth(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "${date.dayOfMonth} $month"
}

private fun guestsLabel(occupancy: Occupancy): String {
    val guests = occupancy.adults + occupancy.children
    val roomsPart = if (occupancy.rooms == 1) "1 room" else "${occupancy.rooms} rooms"
    val guestsPart = if (guests == 1) "1 guest" else "$guests guests"
    return "$roomsPart · $guestsPart"
}
