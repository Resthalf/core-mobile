package com.resthalflab.resthalfapp.feature.search.ui.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationType
import com.resthalflab.resthalfapp.feature.wholesale.api.Occupancy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

interface HomeComponent {
    val state: StateFlow<UiState>

    fun onCitySelected(city: String)
    fun onDateSelected(date: LocalDate)
    fun onSlotTypeSelected(slotType: SlotType)
    fun onOccupancyChanged(occupancy: Occupancy)
    fun onSearchClicked()

    // Location autosuggest sheet
    fun onLocationSheetOpened()
    fun onLocationSheetDismissed()
    fun onLocationQueryChanged(term: String)
    fun onLocationSelected(suggestion: LocationSuggestion)

    data class UiState(
        val city: String,
        val date: LocalDate,
        val slotType: SlotType,
        val occupancy: Occupancy = Occupancy(),
        // Track whether the user has actively picked a date / guests, so the picker rows can show a
        // greyed placeholder prompt until then (the underlying date/occupancy still hold usable defaults).
        val dateChosen: Boolean = false,
        val guestsChosen: Boolean = false,
        val selectedLocation: LocationSuggestion? = null,
        val locationQuery: String = "",
        val locationResults: List<LocationSuggestion> = emptyList(),
        val isSearchingLocation: Boolean = false,
        val locationError: Boolean = false,
    )
}

@OptIn(FlowPreview::class)
class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val locationSearch: LocationSearchApi,
    private val onSearch: (SearchArgs) -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val tz = TimeZone.currentSystemDefault()
    private val scope = coroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(
        HomeComponent.UiState(
            city = "",
            date = Clock.System.todayIn(tz),
            slotType = defaultSlotForNow(),
        )
    )
    override val state: StateFlow<HomeComponent.UiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    /** Last unfiltered response; re-filtered when the slot changes so the two stay consistent. */
    private var rawResults: List<LocationSuggestion> = emptyList()

    init {
        scope.launch {
            queryFlow
                .debounce(DEBOUNCE_MS)
                .map { it.trim() }
                .distinctUntilChanged()
                .collectLatest { term -> runLocationSearch(term) }
        }
    }

    override fun onCitySelected(city: String) =
        _state.update { it.copy(city = city, selectedLocation = null) }

    override fun onDateSelected(date: LocalDate) = _state.update { it.copy(date = date, dateChosen = true) }

    override fun onSlotTypeSelected(slotType: SlotType) =
        _state.update { it.copy(slotType = slotType, locationResults = rawResults.filterForSlot(slotType)) }

    override fun onOccupancyChanged(occupancy: Occupancy) =
        _state.update { it.copy(occupancy = occupancy, guestsChosen = true) }

    override fun onLocationSheetOpened() = resetLocationSearch()

    override fun onLocationSheetDismissed() = resetLocationSearch()

    override fun onLocationQueryChanged(term: String) {
        _state.update { it.copy(locationQuery = term, locationError = false) }
        queryFlow.value = term
    }

    override fun onLocationSelected(suggestion: LocationSuggestion) =
        _state.update { it.copy(city = suggestion.name, selectedLocation = suggestion) }

    override fun onSearchClicked() {
        val s = _state.value
        onSearch(
            SearchArgs(
                city = s.city,
                date = s.date.toString(), // ISO yyyy-MM-dd
                slotType = s.slotType,
                adults = s.occupancy.adults,
                occupancy = s.occupancy,
            )
        )
    }

    private fun resetLocationSearch() {
        rawResults = emptyList()
        queryFlow.value = ""
        _state.update {
            it.copy(
                locationQuery = "",
                locationResults = emptyList(),
                isSearchingLocation = false,
                locationError = false,
            )
        }
    }

    private suspend fun runLocationSearch(term: String) {
        if (term.length < MIN_QUERY_LENGTH) {
            rawResults = emptyList()
            _state.update { it.copy(locationResults = emptyList(), isSearchingLocation = false, locationError = false) }
            return
        }
        _state.update { it.copy(isSearchingLocation = true, locationError = false) }
        when (val result = locationSearch.autosuggest(term)) {
            is AppResult.Success -> {
                rawResults = result.value
                _state.update {
                    it.copy(
                        locationResults = result.value.filterForSlot(it.slotType),
                        isSearchingLocation = false,
                        locationError = false,
                    )
                }
            }
            is AppResult.Failure -> {
                rawResults = emptyList()
                _state.update { it.copy(locationResults = emptyList(), isSearchingLocation = false, locationError = true) }
            }
        }
    }

    // Browsing at night (evening or early morning) defaults to the half-day "night stay";
    // daytime defaults to full-day. The user can still change it via the stay-window sheet.
    private fun defaultSlotForNow(): SlotType {
        val hour = Clock.System.now().toLocalDateTime(tz).hour
        return if (hour >= 18 || hour < 6) SlotType.HALF_DAY else SlotType.FULL_DAY
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
        const val MIN_QUERY_LENGTH = 2
    }
}

/**
 * Half-Day (day rooms) is city-based inventory, so only City suggestions apply; Full-Day (wholesale)
 * can target any location type.
 */
internal fun List<LocationSuggestion>.filterForSlot(slotType: SlotType): List<LocationSuggestion> =
    when (slotType) {
        SlotType.HALF_DAY -> filter { it.type == LocationType.CITY }
        SlotType.FULL_DAY -> this
    }

private fun Clock.System.todayIn(tz: TimeZone): LocalDate = now().toLocalDateTime(tz).date
