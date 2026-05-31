package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.domain.GetListingUseCase

class DefaultListingComponentFactory(
    private val getListing: GetListingUseCase,
) : ListingComponentFactory {
    override fun createDetail(
        componentContext: ComponentContext,
        listingId: String,
        onBack: () -> Unit,
    ): ListingDetailComponent = DefaultListingDetailComponent(
        componentContext = componentContext,
        getListing = getListing,
        listingId = listingId,
        onBack = onBack,
    )
}
