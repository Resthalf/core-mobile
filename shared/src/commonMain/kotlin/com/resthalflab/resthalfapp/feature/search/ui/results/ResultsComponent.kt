package com.resthalflab.resthalfapp.feature.search.ui.results

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.domain.SearchHotelsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.formatLongDate
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult
import com.resthalflab.resthalfapp.feature.search.domain.stayTitle
import com.resthalflab.resthalfapp.feature.search.domain.windowShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ResultsComponent {
    val state: StateFlow<UiState>

    /** Ids of hotels the user has favorited — drives the heart on each result. */
    val favoriteHotelIds: StateFlow<Set<String>>

    fun onRoomSelected(selection: RoomSelection)
    fun onToggleFavorite(hotel: HotelSearchResult)
    fun onBackClicked()
    fun onRetry()

    data class UiState(
        val city: String,
        val dateLabel: String,
        val stayTitle: String,
        val windowShort: String,
        val loading: Boolean = false,
        val results: List<HotelSearchResult> = emptyList(),
        val error: String? = null,
    )
}

class DefaultResultsComponent(
    componentContext: ComponentContext,
    private val searchHotels: SearchHotelsUseCase,
    private val favorites: FavoritesRepository,
    private val args: SearchArgs,
    private val onOpenRoom: (RoomSelection) -> Unit,
    private val onBack: () -> Unit,
) : ResultsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(
        ResultsComponent.UiState(
            city = args.city,
            dateLabel = formatLongDate(args.date),
            stayTitle = args.slotType.stayTitle,
            windowShort = args.slotType.windowShort,
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
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = searchHotels(args)) {
                is AppResult.Success -> _state.update { it.copy(loading = false, results = result.value) }
                is AppResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
            }
        }
    }

    override fun onRoomSelected(selection: RoomSelection) = onOpenRoom(selection)

    override fun onToggleFavorite(hotel: HotelSearchResult) {
        favorites.toggle(
            FavoriteHotel(
                hotelId = hotel.hotelId,
                hotelName = hotel.hotelName,
                city = hotel.city,
                slotLabel = hotel.slotType.stayTitle,
                fromPrice = hotel.fromPrice,
                currency = hotel.currency,
            )
        )
    }

    override fun onBackClicked() = onBack()
    override fun onRetry() = search()
}
