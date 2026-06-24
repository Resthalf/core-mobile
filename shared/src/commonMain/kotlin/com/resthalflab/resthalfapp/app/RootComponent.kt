package com.resthalflab.resthalfapp.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.loginComponent
import com.resthalflab.resthalfapp.feature.auth.registerComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.register.RegisterComponent
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingByIdUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.VacateBookingUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.ui.detail.BookingDetailComponent
import com.resthalflab.resthalfapp.feature.bookings.ui.detail.DefaultBookingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationArgs
import com.resthalflab.resthalfapp.feature.listing.api.BookingConfirmationComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import com.resthalflab.resthalfapp.feature.listing.api.PaymentComponent
import com.resthalflab.resthalfapp.feature.listing.api.RoomSelection
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.domain.SearchHotelsUseCase
import com.resthalflab.resthalfapp.feature.search.ui.results.DefaultResultsComponent
import com.resthalflab.resthalfapp.feature.search.ui.results.ResultsComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.Koin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Login(val component: LoginComponent) : Child
        data class Register(val component: RegisterComponent) : Child
        data class Main(val component: MainComponent) : Child
        data class SearchResults(val component: ResultsComponent) : Child
        data class ListingDetail(val component: ListingDetailComponent) : Child
        data class Payment(val component: PaymentComponent) : Child
        data class BookingConfirmation(val component: BookingConfirmationComponent) : Child
        data class BookingDetail(val component: BookingDetailComponent) : Child
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val auth: AuthApi,
    private val koin: Koin,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val scope = coroutineScope(Dispatchers.Main)

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = initialConfig(),
            handleBackButton = true,
            childFactory = ::child,
        )

    init {
        scope.launch {
            auth.session.drop(1).collect { session ->
                val target = if (session != null) Config.Main else Config.Login
                navigation.navigate { listOf(target) }
            }
        }
    }

    private fun initialConfig(): Config =
        if (auth.session.value != null) Config.Main else Config.Login

    private fun child(config: Config, context: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Login -> RootComponent.Child.Login(
                loginComponent(
                    ctx = context,
                    koin = koin,
                    onNavigateToRegister = { navigation.push(Config.Register) },
                )
            )

            Config.Register -> RootComponent.Child.Register(
                registerComponent(
                    ctx = context,
                    koin = koin,
                    onBack = { navigation.pop() },
                )
            )

            Config.Main -> RootComponent.Child.Main(
                DefaultMainComponent(
                    componentContext = context,
                    koin = koin,
                    accountType = auth.session.value?.accountType ?: AccountType.Guest,
                    onOpenSearchResults = { args -> navigation.push(Config.SearchResults(args)) },
                    onOpenBookingDetail = { id -> navigation.push(Config.BookingDetail(id)) },
                )
            )

            is Config.SearchResults -> RootComponent.Child.SearchResults(
                DefaultResultsComponent(
                    componentContext = context,
                    searchHotels = koin.get<SearchHotelsUseCase>(),
                    args = config.args,
                    onOpenRoom = { selection -> navigation.push(Config.ListingDetail(selection)) },
                    onBack = { navigation.pop() },
                )
            )

            is Config.ListingDetail -> RootComponent.Child.ListingDetail(
                koin.get<ListingComponentFactory>().createDetail(
                    componentContext = context,
                    selection = config.selection,
                    onBack = { navigation.pop() },
                    // Booking created → show the "Booking Created" confirmation first.
                    onBooked = { args -> navigation.push(Config.BookingConfirmation(args, paid = false)) },
                )
            )

            is Config.Payment -> RootComponent.Child.Payment(
                koin.get<ListingComponentFactory>().createPayment(
                    componentContext = context,
                    args = config.args,
                    // After settlement, reset to the tab shell + the paid confirmation (drops the
                    // payment / created-confirmation / listing-detail / pending booking-detail).
                    onPaid = {
                        navigation.navigate { stack ->
                            stackUpToMain(stack) + Config.BookingConfirmation(config.args, paid = true)
                        }
                    },
                    onBack = { navigation.pop() },
                )
            )

            is Config.BookingConfirmation -> RootComponent.Child.BookingConfirmation(
                koin.get<ListingComponentFactory>().createBookingConfirmation(
                    componentContext = context,
                    args = config.args,
                    paid = config.paid,
                    onBack = { navigation.pop() },
                    onProceedToPayment = { navigation.push(Config.Payment(config.args)) },
                    // Reset to the tab shell + the booking detail so we never duplicate a
                    // BookingDetail already on the stack (Decompose requires unique configs).
                    onViewDetails = {
                        navigation.navigate { stack ->
                            stackUpToMain(stack) + Config.BookingDetail(config.args.bookingId)
                        }
                    },
                )
            )

            is Config.BookingDetail -> RootComponent.Child.BookingDetail(
                DefaultBookingDetailComponent(
                    componentContext = context,
                    getBookingById = koin.get<GetBookingByIdUseCase>(),
                    vacateBooking = koin.get<VacateBookingUseCase>(),
                    bookingId = config.id,
                    onBack = { navigation.pop() },
                    onPay = { booking -> navigation.push(Config.Payment(booking.toConfirmationArgs())) },
                )
            )
        }

    /** Everything up to and including the authenticated [Config.Main] shell — used to reset deep stacks. */
    private fun stackUpToMain(stack: List<Config>): List<Config> {
        val mainIndex = stack.indexOfFirst { it is Config.Main }
        return if (mainIndex >= 0) stack.take(mainIndex + 1) else listOf(Config.Main)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Register : Config

        @Serializable
        data object Main : Config

        @Serializable
        data class SearchResults(val args: SearchArgs) : Config

        @Serializable
        data class ListingDetail(val selection: RoomSelection) : Config

        @Serializable
        data class Payment(val args: BookingConfirmationArgs) : Config

        @Serializable
        data class BookingConfirmation(val args: BookingConfirmationArgs, val paid: Boolean) : Config

        @Serializable
        data class BookingDetail(val id: String) : Config
    }
}

// Build the payment/confirmation args from an existing (Pending) booking so the detail screen can
// re-enter the same payment flow.
private fun Booking.toConfirmationArgs(): BookingConfirmationArgs = BookingConfirmationArgs(
    bookingId = id,
    orderId = bookingCode,
    hotelName = hotelName,
    roomNumber = roomNumber,
    slotType = slotType,
    amount = totalPrice,
    currency = currency,
)
