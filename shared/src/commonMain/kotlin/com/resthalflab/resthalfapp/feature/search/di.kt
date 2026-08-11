package com.resthalflab.resthalfapp.feature.search

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.data.DefaultSearchRepository
import com.resthalflab.resthalfapp.feature.search.data.SearchRemote
import com.resthalflab.resthalfapp.feature.search.domain.SearchHotelsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.ui.DefaultSearchTabComponent
import com.resthalflab.resthalfapp.feature.search.ui.SearchTabComponent
import org.koin.core.module.Module
import org.koin.dsl.module

val searchModule: Module = module {
    single { SearchRemote(get()) }
    single<SearchRepository> { DefaultSearchRepository(get()) }
    factory { SearchHotelsUseCase(get()) }
}

/** Results and deeper screens are root destinations — only the home tab is built here. */
fun searchTabComponent(
    componentContext: ComponentContext,
    onOpenSearchResults: (SearchArgs) -> Unit,
): SearchTabComponent = DefaultSearchTabComponent(
    componentContext = componentContext,
    onOpenSearchResults = onOpenSearchResults,
)
