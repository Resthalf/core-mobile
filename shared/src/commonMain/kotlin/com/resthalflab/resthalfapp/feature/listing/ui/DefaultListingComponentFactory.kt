package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase

class DefaultListingComponentFactory(
    private val createBooking: CreateBookingUseCase,
) : ListingComponentFactory {
    override fun createDetail(
        componentContext: ComponentContext,
        selection: RoomSelection,
        onBack: () -> Unit,
        onBooked: (BookingConfirmationArgs) -> Unit,
    ): ListingDetailComponent = DefaultListingDetailComponent(
        componentContext = componentContext,
        selection = selection,
        createBooking = createBooking,
        onBack = onBack,
        onBooked = onBooked,
    )

    override fun createBookingConfirmation(
        componentContext: ComponentContext,
        args: BookingConfirmationArgs,
        onBack: () -> Unit,
    ): BookingConfirmationComponent = DefaultBookingConfirmationComponent(
        componentContext = componentContext,
        args = args,
        onBack = onBack,
    )
}
