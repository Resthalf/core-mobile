package com.resthalflab.resthalfapp.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.data.LocalAuthRepository
import com.resthalflab.resthalfapp.feature.auth.domain.LoginUseCase
import com.resthalflab.resthalfapp.feature.auth.domain.RegisterUseCase
import com.resthalflab.resthalfapp.feature.auth.ui.login.DefaultLoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.register.DefaultRegisterComponent
import com.resthalflab.resthalfapp.feature.auth.ui.register.RegisterComponent
import com.resthalflab.resthalfapp.feature.auth.ui.welcome.DefaultWelcomeComponent
import com.resthalflab.resthalfapp.feature.auth.ui.welcome.WelcomeComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val authModule: Module = module {
    // Active MVP auth: local session (Google + guest), no own backend.
    single<AuthApi> { LocalAuthRepository(get()) }

    // Reference-only (backend phone/password): LoginUseCase/RegisterUseCase resolve the local
    // AuthApi and are not routed in the MVP.
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
}

/** Active entry point: the Google + guest welcome screen. */
fun welcomeComponent(ctx: ComponentContext, koin: Koin): WelcomeComponent =
    DefaultWelcomeComponent(componentContext = ctx, auth = koin.get())

/** Reference-only (backend phone/password); not routed in the MVP. */
fun loginComponent(
    ctx: ComponentContext,
    koin: Koin,
    onNavigateToRegister: () -> Unit,
): LoginComponent = DefaultLoginComponent(
    componentContext = ctx,
    login = koin.get(),
    onNavigateToRegister = onNavigateToRegister,
)

/** Reference-only (backend register); not routed in the MVP. */
fun registerComponent(
    ctx: ComponentContext,
    koin: Koin,
    onBack: () -> Unit,
): RegisterComponent = DefaultRegisterComponent(
    componentContext = ctx,
    register = koin.get(),
    onBack = onBack,
)
