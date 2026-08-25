package com.resthalflab.resthalfapp.feature.search

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.data.OfflineSearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.SearchHotelsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.ui.DefaultSearchTabComponent
import com.resthalflab.resthalfapp.feature.search.ui.SearchTabComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val searchModule: Module = module {
    // Day-room search runs offline while the RestHalf backend is retired (SearchRemote +
    // DefaultSearchRepository are kept in the tree for the future Zentrumhub Nexus migration).
    single<SearchRepository> { OfflineSearchRepository() }
    factory { SearchHotelsUseCase(get()) }
}

/** Results and deeper screens are root destinations — only the home tab is built here. */
fun searchTabComponent(
    componentContext: ComponentContext,
    koin: Koin,
    onOpenSearchResults: (SearchArgs) -> Unit,
): SearchTabComponent {
    val session = koin.get<AuthApi>().session.value
    return DefaultSearchTabComponent(
        componentContext = componentContext,
        locationSearch = koin.get(),
        onOpenSearchResults = onOpenSearchResults,
        userName = session?.displayName.orEmpty(),
        avatarUrl = session?.photoUrl,
    )
}
