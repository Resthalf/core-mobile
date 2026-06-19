package com.resthalflab.resthalfapp.feature.listing.api

import com.arkivanov.decompose.ComponentContext

/**
 * Public entry point for the listing feature. Other features/host depend on this interface only
 * (never on listing's data/domain/ui internals) to present its screens.
 */
interface ListingComponentFactory {
    fun createDetail(
        componentContext: ComponentContext,
        selection: RoomSelection,
        onBack: () -> Unit,
        onBooked: (BookingConfirmationArgs) -> Unit,
    ): ListingDetailComponent

    fun createPayment(
        componentContext: ComponentContext,
        args: BookingConfirmationArgs,
        onPaid: () -> Unit,
        onBack: () -> Unit,
    ): PaymentComponent

    fun createBookingConfirmation(
        componentContext: ComponentContext,
        args: BookingConfirmationArgs,
        paid: Boolean,
        onBack: () -> Unit,
        onProceedToPayment: () -> Unit,
        onViewDetails: () -> Unit,
    ): BookingConfirmationComponent
}
