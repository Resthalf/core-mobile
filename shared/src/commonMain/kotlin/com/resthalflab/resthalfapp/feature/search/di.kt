package com.resthalflab.resthalfapp.feature.search

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.data.DefaultSearchRepository
import com.resthalflab.resthalfapp.feature.search.data.SearchRemote
import com.resthalflab.resthalfapp.feature.search.domain.SearchListingsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.ui.DefaultSearchTabComponent
import com.resthalflab.resthalfapp.feature.search.ui.SearchTabComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val searchModule: Module = module {
    single { SearchRemote(get()) }
    single<SearchRepository> { DefaultSearchRepository(get()) }
    factory { SearchListingsUseCase(get()) }
}

/**
 * Feature entry point: builds the Home/Search tab (home form + results).
 * Opening a listing detail is delegated to [onOpenListing] so the host can present it full-screen.
 */
fun searchTabComponent(
    componentContext: ComponentContext,
    koin: Koin,
    onOpenListing: (String) -> Unit,
): SearchTabComponent =
    DefaultSearchTabComponent(
        componentContext = componentContext,
        searchListings = koin.get(),
        onOpenListing = onOpenListing,
    )
