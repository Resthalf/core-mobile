package com.resthalflab.resthalfapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor2.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.resthalflab.resthalfapp.app.MainComponent
import com.resthalflab.resthalfapp.app.RootComponent
import com.resthalflab.resthalfapp.core.design.ResthalfTheme
import com.resthalflab.resthalfapp.feature.auth.ui.welcome.WelcomeScreen
import com.resthalflab.resthalfapp.feature.bookings.ui.BookingsScreen
import com.resthalflab.resthalfapp.feature.bookings.ui.detail.BookingDetailScreen
import com.resthalflab.resthalfapp.feature.favorites.ui.FavoritesScreen
import com.resthalflab.resthalfapp.feature.listing.ui.BookingConfirmationScreen
import com.resthalflab.resthalfapp.feature.listing.ui.ListingDetailScreen
import com.resthalflab.resthalfapp.feature.listing.ui.PaymentScreen
import com.resthalflab.resthalfapp.feature.profile.ui.ProfileScreen
import com.resthalflab.resthalfapp.feature.search.ui.SearchTab
import com.resthalflab.resthalfapp.feature.search.ui.checkout.CheckoutScreen
import com.resthalflab.resthalfapp.feature.search.ui.detail.HotelDetailScreen
import com.resthalflab.resthalfapp.feature.search.ui.results.ResultsScreen
import com.resthalflab.resthalfapp.feature.staff.ui.checkins.CheckInsScreen
import com.resthalflab.resthalfapp.feature.staff.ui.home.StaffHomeScreen
import com.resthalflab.resthalfapp.feature.staff.ui.rooms.RoomsScreen

@Composable
fun App(rootComponent: RootComponent) {
    // Configure the shared, cached image loader once. The Ktor fetcher makes network loading work on
    // every platform (Android auto-detects it; iOS needs it registered explicitly).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    ResthalfTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Children(stack = rootComponent.childStack) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Login -> WelcomeScreen(instance.component)
                    is RootComponent.Child.Main -> MainScreen(instance.component)
                    is RootComponent.Child.SearchResults -> ResultsScreen(instance.component)
                    is RootComponent.Child.HotelDetail -> HotelDetailScreen(instance.component)
                    is RootComponent.Child.Checkout -> CheckoutScreen(instance.component)
                    is RootComponent.Child.ListingDetail -> ListingDetailScreen(instance.component)
                    is RootComponent.Child.Payment -> PaymentScreen(instance.component)
                    is RootComponent.Child.BookingConfirmation -> BookingConfirmationScreen(instance.component)
                    is RootComponent.Child.BookingDetail -> BookingDetailScreen(instance.component)
                }
            }
        }
    }
}

@Composable
private fun MainScreen(component: MainComponent) {
    val stack by component.stack.subscribeAsState()
    val activeTab = stack.active.instance.tab

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                component.tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { component.onTabSelected(tab) },
                        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
                        label = { Text(tab.label()) },
                    )
                }
            }
        },
    ) { padding ->
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { child ->
            when (val instance = child.instance) {
                is MainComponent.Child.Home -> SearchTab(instance.component)
                is MainComponent.Child.Bookings -> BookingsScreen(instance.component)
                is MainComponent.Child.Favorites -> FavoritesScreen(instance.component)
                is MainComponent.Child.StaffHome -> StaffHomeScreen(instance.component)
                is MainComponent.Child.CheckIns -> CheckInsScreen(instance.component)
                is MainComponent.Child.Rooms -> RoomsScreen(instance.component)
                is MainComponent.Child.Profile -> ProfileScreen(instance.component)
            }
        }
    }
}

private fun MainComponent.Tab.label(): String = when (this) {
    MainComponent.Tab.Home -> "Home"
    MainComponent.Tab.Bookings -> "Bookings"
    MainComponent.Tab.Favorites -> "Favorites"
    MainComponent.Tab.CheckIns -> "Check-ins"
    MainComponent.Tab.Rooms -> "Rooms"
    MainComponent.Tab.Profile -> "Profile"
}

private fun MainComponent.Tab.icon(): ImageVector = when (this) {
    MainComponent.Tab.Home -> Icons.Outlined.Home
    MainComponent.Tab.Bookings -> Icons.Outlined.CalendarMonth
    MainComponent.Tab.Favorites -> Icons.Outlined.FavoriteBorder
    MainComponent.Tab.CheckIns -> Icons.Outlined.HowToReg
    MainComponent.Tab.Rooms -> Icons.Outlined.MeetingRoom
    MainComponent.Tab.Profile -> Icons.Outlined.Person
}
