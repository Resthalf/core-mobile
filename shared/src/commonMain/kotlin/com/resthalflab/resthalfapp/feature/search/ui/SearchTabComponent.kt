package com.resthalflab.resthalfapp.feature.search.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.ui.home.DefaultHomeComponent
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeComponent
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi

/** The Home/Search tab. Results are pushed to the root stack via [onOpenSearchResults]. */
interface SearchTabComponent {
    val home: HomeComponent
}

class DefaultSearchTabComponent(
    componentContext: ComponentContext,
    locationSearch: LocationSearchApi,
    onOpenSearchResults: (SearchArgs) -> Unit,
) : SearchTabComponent, ComponentContext by componentContext {
    override val home: HomeComponent = DefaultHomeComponent(
        componentContext = componentContext,
        locationSearch = locationSearch,
        onSearch = onOpenSearchResults,
    )
}
