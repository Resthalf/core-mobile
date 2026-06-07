package com.resthalflab.resthalfapp.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.resthalflab.resthalfapp.feature.bookings.bookingsComponent
import com.resthalflab.resthalfapp.feature.bookings.ui.BookingsComponent
import com.resthalflab.resthalfapp.feature.favorites.favoritesComponent
import com.resthalflab.resthalfapp.feature.favorites.ui.FavoritesComponent
import com.resthalflab.resthalfapp.feature.profile.profileComponent
import com.resthalflab.resthalfapp.feature.profile.ui.ProfileComponent
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.searchTabComponent
import com.resthalflab.resthalfapp.feature.search.ui.SearchTabComponent
import kotlinx.serialization.Serializable
import org.koin.core.Koin

interface MainComponent {
    val stack: Value<ChildStack<*, Child>>
    fun onTabSelected(tab: Tab)

    enum class Tab { Home, Bookings, Favorites, Profile }

    sealed interface Child {
        val tab: Tab
        data class Home(val component: SearchTabComponent) : Child {
            override val tab = Tab.Home
        }
        data class Bookings(val component: BookingsComponent) : Child {
            override val tab = Tab.Bookings
        }
        data class Favorites(val component: FavoritesComponent) : Child {
            override val tab = Tab.Favorites
        }
        data class Profile(val component: ProfileComponent) : Child {
            override val tab = Tab.Profile
        }
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val koin: Koin,
    private val onOpenListing: (String) -> Unit,
    private val onOpenSearchResults: (SearchArgs) -> Unit,
    private val onOpenBookingDetail: (String) -> Unit,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::child,
        )

    override fun onTabSelected(tab: MainComponent.Tab) {
        navigation.bringToFront(tab.toConfig())
    }

    private fun child(config: Config, context: ComponentContext): MainComponent.Child =
        when (config) {
            Config.Home -> MainComponent.Child.Home(searchTabComponent(context, onOpenSearchResults))
            Config.Bookings -> MainComponent.Child.Bookings(bookingsComponent(context, koin, onOpenBookingDetail))
            Config.Favorites -> MainComponent.Child.Favorites(favoritesComponent(context))
            Config.Profile -> MainComponent.Child.Profile(profileComponent(context, koin))
        }

    private fun MainComponent.Tab.toConfig(): Config = when (this) {
        MainComponent.Tab.Home -> Config.Home
        MainComponent.Tab.Bookings -> Config.Bookings
        MainComponent.Tab.Favorites -> Config.Favorites
        MainComponent.Tab.Profile -> Config.Profile
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Home : Config

        @Serializable
        data object Bookings : Config

        @Serializable
        data object Favorites : Config

        @Serializable
        data object Profile : Config
    }
}
