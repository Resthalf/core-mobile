package com.resthalflab.resthalfapp.feature.search.ui.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.favorites.api.FavoriteHotel
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.resthalflab.resthalfapp.feature.search.api.CheckoutArgs
import com.resthalflab.resthalfapp.feature.search.api.HotelDetailArgs
import com.resthalflab.resthalfapp.feature.wholesale.api.AccommodationRules
import com.resthalflab.resthalfapp.feature.wholesale.api.NearbyAttraction
import com.resthalflab.resthalfapp.feature.wholesale.api.ReviewCategory
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomOffer
import com.resthalflab.resthalfapp.feature.wholesale.api.RoomRateOption
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleDetailApi
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
import kotlinx.datetime.daysUntil

interface HotelDetailComponent {
    val state: StateFlow<UiState>

    /** Whether this hotel is saved — drives the heart in the toolbar. */
    val isFavorite: StateFlow<Boolean>

    fun onBackClicked()
    fun onRetry()
    fun onToggleFavorite()
    fun onSelectOption(room: RoomOffer, option: RoomRateOption)

    data class UiState(
        val hotelName: String,
        val heroImage: String?,
        val starRating: Int?,
        val category: String?,
        val address: String?,
        val reviewRating: Double?,
        val reviewCount: Int?,
        val aboutText: String?,
        val highlights: List<String>,
        val allHighlights: List<String>,
        val reviewCategories: List<ReviewCategory>,
        val recommendPercent: Double?,
        val popularFacilities: List<String>,
        val locationAddress: String?,
        val geoLat: String?,
        val geoLong: String?,
        val nearbyAttractions: List<NearbyAttraction>,
        val rules: AccommodationRules?,
        val rooms: List<RoomOffer>,
        val currency: String,
        val fromPrice: Int?,
        val nextToken: String? = null,
        val loading: Boolean = true,
        val error: String? = null,
    )
}

class DefaultHotelDetailComponent(
    componentContext: ComponentContext,
    private val detailApi: WholesaleDetailApi,
    private val favorites: FavoritesRepository,
    private val args: HotelDetailArgs,
    private val onBack: () -> Unit,
    private val onOpenCheckout: (CheckoutArgs) -> Unit,
) : HotelDetailComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(
        HotelDetailComponent.UiState(
            hotelName = args.hotelName,
            heroImage = args.heroImage,
            starRating = args.starRating,
            category = args.category,
            address = args.address,
            reviewRating = args.reviewRating,
            reviewCount = args.reviewCount,
            aboutText = null,
            highlights = emptyList(),
            allHighlights = emptyList(),
            reviewCategories = emptyList(),
            recommendPercent = null,
            popularFacilities = emptyList(),
            locationAddress = null,
            geoLat = null,
            geoLong = null,
            nearbyAttractions = emptyList(),
            rules = null,
            rooms = emptyList(),
            currency = args.currency,
            fromPrice = null,
            loading = true,
        )
    )
    override val state: StateFlow<HotelDetailComponent.UiState> = _state.asStateFlow()

    override val isFavorite: StateFlow<Boolean> =
        favorites.favorites
            .map { list -> list.any { it.hotelId == args.hotelId } }
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                favorites.favorites.value.any { it.hotelId == args.hotelId },
            )

    init {
        load()
    }

    private fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = detailApi.loadDetail(args.hotelId, args.searchToken, args.checkIn, args.checkOut)) {
                is AppResult.Success -> {
                    val detail = result.value
                    val fromPrice = detail.rooms
                        .mapNotNull { room -> room.options.minOfOrNull { it.perNightRate } }
                        .minOrNull()
                    _state.update {
                        it.copy(
                            loading = false,
                            error = null,
                            hotelName = detail.name,
                            heroImage = detail.heroImage ?: it.heroImage,
                            starRating = detail.starRating ?: it.starRating,
                            category = detail.category ?: it.category,
                            address = detail.address ?: it.address,
                            reviewRating = detail.reviewRating ?: it.reviewRating,
                            reviewCount = detail.reviewCount ?: it.reviewCount,
                            aboutText = detail.aboutText,
                            highlights = detail.highlights,
                            allHighlights = detail.allHighlights,
                            reviewCategories = detail.reviewCategories,
                            recommendPercent = detail.recommendPercent,
                            popularFacilities = detail.popularFacilities,
                            locationAddress = detail.locationAddress,
                            geoLat = detail.geoLat,
                            geoLong = detail.geoLong,
                            nearbyAttractions = detail.nearbyAttractions,
                            rules = detail.rules,
                            rooms = detail.rooms,
                            currency = detail.currency,
                            fromPrice = fromPrice,
                            nextToken = detail.nextToken,
                        )
                    }
                }

                is AppResult.Failure -> _state.update {
                    it.copy(loading = false, error = result.error.message)
                }
            }
        }
    }

    override fun onBackClicked() = onBack()

    override fun onRetry() = load()

    override fun onToggleFavorite() {
        val current = _state.value
        favorites.toggle(
            FavoriteHotel(
                hotelId = args.hotelId,
                hotelName = current.hotelName,
                city = current.address ?: "",
                slotLabel = current.category ?: "Hotel",
                fromPrice = current.fromPrice ?: 0,
                currency = current.currency,
                imageUrl = args.heroImage,
                rating = args.starRating?.toDouble(),
                location = args.location,
            )
        )
    }

    override fun onSelectOption(room: RoomOffer, option: RoomRateOption) {
        val token = _state.value.nextToken?.ifBlank { null } ?: return
        val recommendationId = option.recommendationId?.ifBlank { null } ?: return
        val nights = runCatching {
            LocalDate.parse(args.checkIn).daysUntil(LocalDate.parse(args.checkOut)).coerceAtLeast(1)
        }.getOrDefault(1)
        onOpenCheckout(
            CheckoutArgs(
                hotelId = args.hotelId,
                token = token,
                recommendationId = recommendationId,
                rateId = option.rateId,
                hotelName = _state.value.hotelName,
                heroImage = _state.value.heroImage,
                location = _state.value.address,
                roomName = room.name,
                boardBasisLabel = option.boardBasisLabel,
                breakfastIncluded = option.breakfastIncluded,
                refundable = option.refundable,
                checkIn = args.checkIn,
                checkOut = args.checkOut,
                nights = nights,
                totalRate = option.totalRate,
                perNightRate = option.perNightRate,
                currency = option.currency,
            )
        )
    }
}
