package com.resthalflab.resthalfapp.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.core.network.AUTH_HTTP_CLIENT
import com.resthalflab.resthalfapp.core.network.TokenProvider
import com.resthalflab.resthalfapp.core.storage.SecureTokenStore
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.core.storage.SettingsSecureTokenStore
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.data.AuthRemote
import com.resthalflab.resthalfapp.feature.auth.data.AuthRepository
import com.resthalflab.resthalfapp.feature.auth.data.AuthTokenProvider
import com.resthalflab.resthalfapp.feature.auth.domain.LoginUseCase
import com.resthalflab.resthalfapp.feature.auth.domain.RegisterUseCase
import com.resthalflab.resthalfapp.feature.auth.ui.login.DefaultLoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.register.DefaultRegisterComponent
import com.resthalflab.resthalfapp.feature.auth.ui.register.RegisterComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule: Module = module {
    single<SecureTokenStore> {
        SettingsSecureTokenStore(get<SettingsFactory>().create("resthalf.auth"))
    }
    // Uses the bare auth client (no Bearer plugin); login/register don't need a token.
    single { AuthRemote(get(named(AUTH_HTTP_CLIENT))) }
    single<TokenProvider> { AuthTokenProvider(get()) }
    single<AuthApi> { AuthRepository(get(), get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
}

fun loginComponent(
    ctx: ComponentContext,
    koin: Koin,
    onNavigateToRegister: () -> Unit,
): LoginComponent = DefaultLoginComponent(
    componentContext = ctx,
    login = koin.get(),
    onNavigateToRegister = onNavigateToRegister,
)

fun registerComponent(
    ctx: ComponentContext,
    koin: Koin,
    onBack: () -> Unit,
): RegisterComponent = DefaultRegisterComponent(
    componentContext = ctx,
    register = koin.get(),
    onBack = onBack,
)
