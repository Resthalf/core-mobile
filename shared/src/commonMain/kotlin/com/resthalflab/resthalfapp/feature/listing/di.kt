package com.resthalflab.resthalfapp.feature.listing

import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.data.BookingRemote
import com.resthalflab.resthalfapp.feature.listing.data.PaymentRemote
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase
import com.resthalflab.resthalfapp.feature.listing.domain.SimulatePaymentUseCase
import com.resthalflab.resthalfapp.feature.listing.ui.DefaultListingComponentFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val listingModule: Module = module {
    single { BookingRemote(get()) }
    single { PaymentRemote(get()) }
    factory { CreateBookingUseCase(get()) }
    factory { SimulatePaymentUseCase(get()) }
    single<ListingComponentFactory> { DefaultListingComponentFactory(get(), get(), get()) }
}
