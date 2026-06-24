package com.resthalflab.resthalfapp.feature.listing.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.PaymentComponent
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.core.storage.FailedBookingStore
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase
import com.resthalflab.resthalfapp.feature.listing.domain.SimulatePaymentUseCase

class DefaultListingComponentFactory(
    private val createBooking: CreateBookingUseCase,
    private val simulatePayment: SimulatePaymentUseCase,
    private val failedBookingStore: FailedBookingStore,
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

    override fun createPayment(
        componentContext: ComponentContext,
        args: BookingConfirmationArgs,
        onPaid: () -> Unit,
        onBack: () -> Unit,
    ): PaymentComponent = DefaultPaymentComponent(
        componentContext = componentContext,
        args = args,
        simulatePayment = simulatePayment,
        failedBookingStore = failedBookingStore,
        onPaid = onPaid,
        onBack = onBack,
    )

    override fun createBookingConfirmation(
        componentContext: ComponentContext,
        args: BookingConfirmationArgs,
        paid: Boolean,
        onBack: () -> Unit,
        onProceedToPayment: () -> Unit,
        onViewDetails: () -> Unit,
    ): BookingConfirmationComponent = DefaultBookingConfirmationComponent(
        componentContext = componentContext,
        args = args,
        paid = paid,
        onBack = onBack,
        proceedToPayment = onProceedToPayment,
        viewDetails = onViewDetails,
    )
}
