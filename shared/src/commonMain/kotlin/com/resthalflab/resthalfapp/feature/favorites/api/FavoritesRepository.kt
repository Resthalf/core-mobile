package com.resthalflab.resthalfapp.feature.favorites.api

import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * A hotel the user saved. Stays mostly flat so it serializes cleanly to storage; the one nested type
 * is [location], a serializable API transport type (already used to ride nav configs). We keep it so
 * "Plan Trip" from Favorites can re-run a real Nexus search (which needs coordinates/apiType, not just
 * a city string). All the extra fields are nullable + defaulted, so favorites saved before they
 * existed still deserialize.
 */
@Serializable
data class FavoriteHotel(
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val slotLabel: String,
    val fromPrice: Int,
    val currency: String,
    val imageUrl: String? = null,
    val rating: Double? = null,
    /** The destination this hotel was found under — lets Favorites re-fire an availability search. */
    val location: LocationSuggestion? = null,
)

/** Local, device-persisted favorites. Public surface so other features (search) can toggle them. */
interface FavoritesRepository {
    val favorites: StateFlow<List<FavoriteHotel>>
    fun isFavorite(hotelId: String): Boolean
    fun toggle(hotel: FavoriteHotel)
    fun remove(hotelId: String)
}
