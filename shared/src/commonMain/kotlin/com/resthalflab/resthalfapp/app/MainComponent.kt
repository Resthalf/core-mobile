package com.resthalflab.resthalfapp.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.bookings.bookingsComponent
import com.resthalflab.resthalfapp.feature.bookings.ui.BookingsComponent
import com.resthalflab.resthalfapp.feature.favorites.favoritesComponent
import com.resthalflab.resthalfapp.feature.favorites.ui.FavoritesComponent
import com.resthalflab.resthalfapp.feature.profile.profileComponent
import com.resthalflab.resthalfapp.feature.profile.ui.ProfileComponent
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.searchTabComponent
import com.resthalflab.resthalfapp.feature.search.ui.SearchTabComponent
import com.resthalflab.resthalfapp.feature.staff.checkInsComponent
import com.resthalflab.resthalfapp.feature.staff.roomsComponent
import com.resthalflab.resthalfapp.feature.staff.staffHomeComponent
import com.resthalflab.resthalfapp.feature.staff.ui.checkins.CheckInsComponent
import com.resthalflab.resthalfapp.feature.staff.ui.home.StaffHomeComponent
import com.resthalflab.resthalfapp.feature.staff.ui.rooms.RoomsComponent
import kotlinx.serialization.Serializable
import org.koin.core.Koin

interface MainComponent {
    val stack: Value<ChildStack<*, Child>>

    /** Bottom-nav tabs in display order — differs by account type. */
    val tabs: List<Tab>

    fun onTabSelected(tab: Tab)

    enum class Tab { Home, Bookings, Favorites, CheckIns, Rooms, Profile }

    sealed interface Child {
        val tab: Tab
        // Guest
        data class Home(val component: SearchTabComponent) : Child {
            override val tab = Tab.Home
        }
        data class Bookings(val component: BookingsComponent) : Child {
            override val tab = Tab.Bookings
        }
        data class Favorites(val component: FavoritesComponent) : Child {
            override val tab = Tab.Favorites
        }
        // Staff
        data class StaffHome(val component: StaffHomeComponent) : Child {
            override val tab = Tab.Home
        }
        data class CheckIns(val component: CheckInsComponent) : Child {
            override val tab = Tab.CheckIns
        }
        data class Rooms(val component: RoomsComponent) : Child {
            override val tab = Tab.Rooms
        }
        // Shared
        data class Profile(val component: ProfileComponent) : Child {
            override val tab = Tab.Profile
        }
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val koin: Koin,
    private val accountType: AccountType,
    private val onOpenSearchResults: (SearchArgs) -> Unit,
    private val onOpenBookingDetail: (String) -> Unit,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val isStaff get() = accountType == AccountType.Staff

    override val tabs: List<MainComponent.Tab> =
        if (isStaff) {
            listOf(
                MainComponent.Tab.Home,
                MainComponent.Tab.CheckIns,
                MainComponent.Tab.Rooms,
                MainComponent.Tab.Profile,
            )
        } else {
            listOf(
                MainComponent.Tab.Home,
                MainComponent.Tab.Bookings,
                MainComponent.Tab.Favorites,
                MainComponent.Tab.Profile,
            )
        }

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
            Config.Home ->
                if (isStaff) {
                    MainComponent.Child.StaffHome(
                        staffHomeComponent(
                            componentContext = context,
                            koin = koin,
                            onOpenSearchResults = onOpenSearchResults,
                            onOpenRooms = { navigation.bringToFront(Config.Rooms) },
                        )
                    )
                } else {
                    MainComponent.Child.Home(searchTabComponent(context, onOpenSearchResults))
                }
            Config.Bookings -> MainComponent.Child.Bookings(bookingsComponent(context, koin, onOpenBookingDetail))
            Config.Favorites -> MainComponent.Child.Favorites(favoritesComponent(context))
            Config.CheckIns -> MainComponent.Child.CheckIns(checkInsComponent(context, koin))
            Config.Rooms -> MainComponent.Child.Rooms(roomsComponent(context, koin))
            Config.Profile -> MainComponent.Child.Profile(profileComponent(context, koin))
        }

    private fun MainComponent.Tab.toConfig(): Config = when (this) {
        MainComponent.Tab.Home -> Config.Home
        MainComponent.Tab.Bookings -> Config.Bookings
        MainComponent.Tab.Favorites -> Config.Favorites
        MainComponent.Tab.CheckIns -> Config.CheckIns
        MainComponent.Tab.Rooms -> Config.Rooms
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
        data object CheckIns : Config

        @Serializable
        data object Rooms : Config

        @Serializable
        data object Profile : Config
    }
}
