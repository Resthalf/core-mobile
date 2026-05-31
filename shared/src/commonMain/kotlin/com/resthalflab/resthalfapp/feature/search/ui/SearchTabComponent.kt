package com.resthalflab.resthalfapp.feature.search.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.resthalflab.resthalfapp.feature.search.domain.SearchListingsUseCase
import com.resthalflab.resthalfapp.feature.search.ui.home.DefaultHomeComponent
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeComponent
import com.resthalflab.resthalfapp.feature.search.ui.results.DefaultResultsComponent
import com.resthalflab.resthalfapp.feature.search.ui.results.ResultsComponent
import kotlinx.serialization.Serializable

interface SearchTabComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Home(val component: HomeComponent) : Child
        data class Results(val component: ResultsComponent) : Child
    }
}

class DefaultSearchTabComponent(
    componentContext: ComponentContext,
    private val searchListings: SearchListingsUseCase,
    private val onOpenListing: (String) -> Unit,
) : SearchTabComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, SearchTabComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, context: ComponentContext): SearchTabComponent.Child =
        when (config) {
            Config.Home -> SearchTabComponent.Child.Home(
                DefaultHomeComponent(
                    componentContext = context,
                    onSearch = { destination -> navigation.push(Config.Results(destination)) },
                )
            )
            is Config.Results -> SearchTabComponent.Child.Results(
                DefaultResultsComponent(
                    componentContext = context,
                    searchListings = searchListings,
                    destination = config.destination,
                    onListingSelected = onOpenListing,
                    onBack = { navigation.pop() },
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Home : Config

        @Serializable
        data class Results(val destination: String) : Config
    }
}
