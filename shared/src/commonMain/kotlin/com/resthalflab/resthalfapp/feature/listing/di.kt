package com.resthalflab.resthalfapp.feature.listing

import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.data.DefaultListingRepository
import com.resthalflab.resthalfapp.feature.listing.data.ListingRemote
import com.resthalflab.resthalfapp.feature.listing.domain.GetListingUseCase
import com.resthalflab.resthalfapp.feature.listing.domain.ListingRepository
import com.resthalflab.resthalfapp.feature.listing.ui.DefaultListingComponentFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val listingModule: Module = module {
    single { ListingRemote(get()) }
    single<ListingRepository> { DefaultListingRepository(get()) }
    factory { GetListingUseCase(get()) }
    single<ListingComponentFactory> { DefaultListingComponentFactory(get()) }
}
