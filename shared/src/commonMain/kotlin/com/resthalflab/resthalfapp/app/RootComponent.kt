package com.resthalflab.resthalfapp.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.loginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.Koin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Login(val component: LoginComponent) : Child
        data class Home(val component: HomeComponent) : Child
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
                val target = if (session != null) Config.Home else Config.Login
                navigation.navigate { listOf(target) }
            }
        }
    }

    private fun initialConfig(): Config =
        if (auth.session.value != null) Config.Home else Config.Login

    private fun child(config: Config, context: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Login -> RootComponent.Child.Login(loginComponent(context, koin))
            Config.Home -> RootComponent.Child.Home(DefaultHomeComponent(context, auth))
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Home : Config
    }
}
