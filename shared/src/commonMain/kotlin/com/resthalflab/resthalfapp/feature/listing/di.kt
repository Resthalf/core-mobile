package com.resthalflab.resthalfapp.feature.listing

import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.data.BookingRemote
import com.resthalflab.resthalfapp.feature.listing.domain.CreateBookingUseCase
import com.resthalflab.resthalfapp.feature.listing.ui.DefaultListingComponentFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val listingModule: Module = module {
    single { BookingRemote(get()) }
    factory { CreateBookingUseCase(get()) }
    single<ListingComponentFactory> { DefaultListingComponentFactory(get()) }
}
