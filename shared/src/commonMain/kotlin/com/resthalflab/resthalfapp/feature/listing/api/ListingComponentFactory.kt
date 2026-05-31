package com.resthalflab.resthalfapp.feature.listing.api

import com.arkivanov.decompose.ComponentContext

/**
 * Public entry point for the listing feature. Other features depend on this interface only
 * (never on listing's data/domain/ui internals) to open a listing detail screen.
 */
interface ListingComponentFactory {
    fun createDetail(
        componentContext: ComponentContext,
        listingId: String,
        onBack: () -> Unit,
    ): ListingDetailComponent
}
