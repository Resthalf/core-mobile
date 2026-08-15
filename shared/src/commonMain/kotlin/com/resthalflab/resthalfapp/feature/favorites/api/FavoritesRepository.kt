package com.resthalflab.resthalfapp.feature.favorites.api

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/** A hotel the user saved. Kept flat (no cross-feature types) so it serializes cleanly to storage. */
@Serializable
data class FavoriteHotel(
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val slotLabel: String,
    val fromPrice: Int,
    val currency: String,
)

/** Local, device-persisted favorites. Public surface so other features (search) can toggle them. */
interface FavoritesRepository {
    val favorites: StateFlow<List<FavoriteHotel>>
    fun isFavorite(hotelId: String): Boolean
    fun toggle(hotel: FavoriteHotel)
    fun remove(hotelId: String)
}
