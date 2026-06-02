package com.resthalflab.resthalfapp.feature.search.ui

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.ui.home.DefaultHomeComponent
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeComponent

/** The Home/Search tab. Results are pushed to the root stack via [onOpenSearchResults]. */
interface SearchTabComponent {
    val home: HomeComponent
}

class DefaultSearchTabComponent(
    componentContext: ComponentContext,
    onOpenSearchResults: (String) -> Unit,
) : SearchTabComponent, ComponentContext by componentContext {
    override val home: HomeComponent = DefaultHomeComponent(
        componentContext = componentContext,
        onSearch = onOpenSearchResults,
    )
}
