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
import com.resthalflab.resthalfapp.feature.auth.ui.login.DefaultLoginComponent
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginComponent
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule: Module = module {
    single<SecureTokenStore> {
        SettingsSecureTokenStore(get<SettingsFactory>().create("resthalf.auth"))
    }
    // Uses the bare auth client (no Bearer plugin) so login/refresh don't recurse through refresh.
    single { AuthRemote(get(named(AUTH_HTTP_CLIENT))) }
    single<TokenProvider> { AuthTokenProvider(get(), get()) }
    single<AuthApi> { AuthRepository(get(), get()) }
    factory { LoginUseCase(get()) }
    factory<LoginComponent> { (ctx: ComponentContext) ->
        DefaultLoginComponent(ctx, get())
    }
}

internal fun loginComponent(ctx: ComponentContext, koin: org.koin.core.Koin): LoginComponent =
    koin.get { parametersOf(ctx) }
