package com.resthalflab.resthalfapp.feature.favorites.data

import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Favorites persisted as a JSON list in [Settings]. New saves go to the front of the list. */
class SettingsFavoritesRepository(
    private val settings: Settings,
) : FavoritesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _favorites = MutableStateFlow(load())
    override val favorites: StateFlow<List<FavoriteHotel>> = _favorites.asStateFlow()

    override fun isFavorite(hotelId: String): Boolean = _favorites.value.any { it.hotelId == hotelId }

    override fun toggle(hotel: FavoriteHotel) {
        val current = _favorites.value
        val next = if (current.any { it.hotelId == hotel.hotelId }) {
            current.filterNot { it.hotelId == hotel.hotelId }
        } else {
            listOf(hotel) + current
        }
        persist(next)
    }

    override fun remove(hotelId: String) {
        persist(_favorites.value.filterNot { it.hotelId == hotelId })
    }

    private fun persist(list: List<FavoriteHotel>) {
        _favorites.value = list
        settings.putString(KEY, json.encodeToString(list))
    }

    private fun load(): List<FavoriteHotel> =
        settings.getStringOrNull(KEY)
            ?.let { runCatching { json.decodeFromString<List<FavoriteHotel>>(it) }.getOrNull() }
            ?: emptyList()

    private companion object {
        const val KEY = "favorites.hotels"
    }
}
