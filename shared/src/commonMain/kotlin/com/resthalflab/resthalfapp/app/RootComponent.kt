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
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.loginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import com.resthalflab.resthalfapp.feature.listing.api.ListingComponentFactory
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetailComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.Koin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Login(val component: LoginComponent) : Child
        data class Main(val component: MainComponent) : Child
        data class ListingDetail(val component: ListingDetailComponent) : Child
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
            Config.Login -> RootComponent.Child.Login(loginComponent(context, koin))
            Config.Main -> RootComponent.Child.Main(
                DefaultMainComponent(
                    componentContext = context,
                    koin = koin,
                    onOpenListing = { id -> navigation.push(Config.ListingDetail(id)) },
                )
            )
            is Config.ListingDetail -> RootComponent.Child.ListingDetail(
                koin.get<ListingComponentFactory>().createDetail(
                    componentContext = context,
                    listingId = config.id,
                    onBack = { navigation.pop() },
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Main : Config

        @Serializable
        data class ListingDetail(val id: String) : Config
    }
}
