package com.resthalflab.resthalfapp.feature.favorites.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow

interface FavoritesComponent {
    val favorites: StateFlow<List<FavoriteHotel>>
    fun onRemove(hotelId: String)
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    private val repository: FavoritesRepository,
) : FavoritesComponent, ComponentContext by componentContext {
    override val favorites: StateFlow<List<FavoriteHotel>> = repository.favorites
    override fun onRemove(hotelId: String) = repository.remove(hotelId)
}
