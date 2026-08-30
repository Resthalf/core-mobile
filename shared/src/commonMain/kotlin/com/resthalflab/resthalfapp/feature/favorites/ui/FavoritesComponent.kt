package com.resthalflab.resthalfapp.feature.favorites.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.resthalflab.resthalfapp.feature.search.api.HotelDetailArgs
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleSearchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

interface FavoritesComponent {
    val favorites: StateFlow<List<FavoriteHotel>>

    /** Progress of a "Plan Trip" action (running the search that unlocks the hotel's rooms). */
    val planning: StateFlow<PlanningState>

    fun onRemove(hotelId: String)

    /** Whether "Plan Trip" can run for this favorite (needs a stored, searchable location). */
    fun canPlanTrip(favorite: FavoriteHotel): Boolean

    /** Open [favorite]'s detail page directly for the chosen dates + 1 guest (like the results "View"). */
    fun onPlanTrip(favorite: FavoriteHotel, checkIn: LocalDate, checkOut: LocalDate)

    fun onDismissPlanningError()

    data class PlanningState(
        /** The favorite whose rooms are being fetched, or null when idle. */
        val loadingHotelId: String? = null,
        val error: String? = null,
    )
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    private val repository: FavoritesRepository,
    private val wholesaleSearch: WholesaleSearchApi,
    private val onOpenHotelDetail: (HotelDetailArgs) -> Unit,
) : FavoritesComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)

    override val favorites: StateFlow<List<FavoriteHotel>> = repository.favorites

    private val _planning = MutableStateFlow(FavoritesComponent.PlanningState())
    override val planning: StateFlow<FavoritesComponent.PlanningState> = _planning.asStateFlow()

    override fun onRemove(hotelId: String) = repository.remove(hotelId)

    override fun canPlanTrip(favorite: FavoriteHotel): Boolean = favorite.location != null

    override fun onPlanTrip(favorite: FavoriteHotel, checkIn: LocalDate, checkOut: LocalDate) {
        val location = favorite.location ?: return
        if (_planning.value.loadingHotelId != null) return
        scope.launch {
            _planning.value = FavoritesComponent.PlanningState(loadingHotelId = favorite.hotelId)
            // Rooms & rates need a live search-session token, so run the availability search for the
            // saved destination first, then open the hotel's detail directly (skipping the results list).
            // The header fields are placeholders; the detail screen reloads them from getHotelContent.
            when (val result = wholesaleSearch.searchHotels(location, checkIn.toString(), checkOut.toString(), Occupancy())) {
                is AppResult.Success -> {
                    onOpenHotelDetail(
                        HotelDetailArgs(
                            hotelId = favorite.hotelId,
                            hotelName = favorite.hotelName,
                            heroImage = favorite.imageUrl,
                            starRating = favorite.rating?.toInt(),
                            address = favorite.city,
                            category = favorite.slotLabel,
                            currency = favorite.currency,
                            searchToken = result.value.token,
                            checkIn = checkIn.toString(),
                            checkOut = checkOut.toString(),
                            location = location,
                        )
                    )
                    _planning.value = FavoritesComponent.PlanningState()
                }

                is AppResult.Failure -> _planning.value =
                    FavoritesComponent.PlanningState(error = result.error.message)
            }
        }
    }

    override fun onDismissPlanningError() {
        _planning.value = _planning.value.copy(error = null)
    }
}
