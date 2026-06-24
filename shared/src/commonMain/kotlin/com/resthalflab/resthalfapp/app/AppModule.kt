package com.resthalflab.resthalfapp.app

import com.resthalflab.resthalfapp.core.domain.Logger
import com.resthalflab.resthalfapp.core.domain.platformLogger
import com.resthalflab.resthalfapp.core.network.AUTH_HTTP_CLIENT
import com.resthalflab.resthalfapp.core.network.HttpClientFactory
import com.resthalflab.resthalfapp.core.network.NetworkConfig
import com.resthalflab.resthalfapp.core.network.TokenProvider
import com.resthalflab.resthalfapp.core.storage.FailedBookingStore
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.core.storage.SettingsFailedBookingStore
import com.resthalflab.resthalfapp.core.storage.defaultSettingsFactory
import com.resthalflab.resthalfapp.feature.auth.authModule
import com.resthalflab.resthalfapp.feature.bookings.bookingsModule
import com.resthalflab.resthalfapp.feature.listing.listingModule
import com.resthalflab.resthalfapp.feature.search.searchModule
import com.resthalflab.resthalfapp.feature.staff.staffModule
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val coreModule: Module = module {
    single<Logger> { platformLogger() }
}

private val storageModule: Module = module {
    single<SettingsFactory> { defaultSettingsFactory() }
    single<FailedBookingStore> { SettingsFailedBookingStore(get<SettingsFactory>().create("resthalf.bookings")) }
}

private val networkModule: Module = module {
    // Trailing slash is required so relative request paths ("auth/guest/login") resolve correctly.
    single { NetworkConfig(baseUrl = "https://resthalf-backend-production.up.railway.app/") }
    single { HttpClientFactory(get(), get()) }
    // Bare client for auth endpoints (no Bearer plugin) — breaks the auth <-> client DI cycle.
    single<HttpClient>(named(AUTH_HTTP_CLIENT)) { get<HttpClientFactory>().createAuthClient() }
    // Default authenticated client used by all feature APIs.
    single<HttpClient> { get<HttpClientFactory>().createApiClient(get<TokenProvider>()) }
}

val appModules: List<Module> = listOf(
    coreModule,
    storageModule,
    networkModule,
    authModule,
    bookingsModule,
    searchModule,
    listingModule,
    staffModule,
)
