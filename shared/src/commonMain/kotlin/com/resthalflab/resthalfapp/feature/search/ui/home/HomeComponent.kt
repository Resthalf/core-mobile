package com.resthalflab.resthalfapp.feature.search.ui.home

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

interface HomeComponent {
    val state: StateFlow<UiState>
    fun onCitySelected(city: String)
    fun onDateSelected(date: LocalDate)
    fun onSlotTypeSelected(slotType: SlotType)
    fun onSearchClicked()

    data class UiState(
        val city: String,
        val date: LocalDate,
        val slotType: SlotType,
    )
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onSearch: (SearchArgs) -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val tz = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        HomeComponent.UiState(
            city = "Jakarta",
            date = Clock.System.todayIn(tz),
            slotType = defaultSlotForNow(),
        )
    )
    override val state: StateFlow<HomeComponent.UiState> = _state.asStateFlow()

    override fun onCitySelected(city: String) = _state.update { it.copy(city = city) }
    override fun onDateSelected(date: LocalDate) = _state.update { it.copy(date = date) }
    override fun onSlotTypeSelected(slotType: SlotType) = _state.update { it.copy(slotType = slotType) }

    override fun onSearchClicked() {
        val s = _state.value
        onSearch(
            SearchArgs(
                city = s.city,
                date = s.date.toString(), // ISO yyyy-MM-dd
                slotType = s.slotType,
            )
        )
    }

    // Browsing at night (evening or early morning) defaults to the half-day "night stay";
    // daytime defaults to full-day. The user can still change it via the stay-window sheet.
    private fun defaultSlotForNow(): SlotType {
        val hour = Clock.System.now().toLocalDateTime(tz).hour
        return if (hour >= 18 || hour < 6) SlotType.HALF_DAY else SlotType.FULL_DAY
    }
}

private fun Clock.System.todayIn(tz: TimeZone): LocalDate = now().toLocalDateTime(tz).date
