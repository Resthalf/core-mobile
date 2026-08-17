package com.resthalflab.resthalfapp.feature.listing

import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase
import com.resthalflab.resthalfapp.feature.listing.domain.SimulatePaymentUseCase
import com.resthalflab.resthalfapp.feature.listing.ui.DefaultListingComponentFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val listingModule: Module = module {
    // Booking creation + payment are local now: they write to the on-device booking store
    // (BookingRemote + PaymentRemote stay in the tree for the future Nexus/payment migration).
    factory { CreateBookingUseCase(get()) }
    factory { SimulatePaymentUseCase(get()) }
    single<ListingComponentFactory> { DefaultListingComponentFactory(get(), get(), get()) }
}
