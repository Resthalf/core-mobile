package com.resthalflab.resthalfapp.app

import com.resthalflab.resthalfapp.core.domain.Logger
import com.resthalflab.resthalfapp.core.domain.isDebugBuild
import com.resthalflab.resthalfapp.core.domain.platformLogger
import com.resthalflab.resthalfapp.core.network.HttpClientFactory
import com.resthalflab.resthalfapp.core.network.NetworkConfig
import com.resthalflab.resthalfapp.core.network.ZENTRUMHUB_AUTOSUGGEST_CLIENT
import com.resthalflab.resthalfapp.core.network.ZENTRUMHUB_NEXUS_CLIENT
import com.resthalflab.resthalfapp.core.network.ZentrumhubBuildConfig
import com.resthalflab.resthalfapp.core.network.ZentrumhubConfig
import com.resthalflab.resthalfapp.core.storage.FailedBookingStore
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.core.storage.SettingsFailedBookingStore
import com.resthalflab.resthalfapp.core.storage.defaultSettingsFactory
import com.resthalflab.resthalfapp.feature.auth.authModule
import com.resthalflab.resthalfapp.feature.bookings.bookingsModule
import com.resthalflab.resthalfapp.feature.favorites.favoritesModule
import com.resthalflab.resthalfapp.feature.listing.listingModule
import com.resthalflab.resthalfapp.feature.search.searchModule
import com.resthalflab.resthalfapp.feature.wholesale.wholesaleModule
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
    // RestHalf backend config — DISABLED (kept for a future phone/password comeback). No client is
    // registered against it below, so its base URL is never used at runtime.
    single { NetworkConfig(baseUrl = "https://resthalf-backend-production.up.railway.app/", logRequests = isDebugBuild()) }
    single {
        ZentrumhubConfig(
            autosuggestBaseUrl = "https://autosuggest.travel.zentrumhub.com/",
            nexusBaseUrl = "https://nexus.prod.zentrumhub.com/",
            // Credentials come from build config (Gradle properties). Autosuggest needs none; these are
            // only used by the (not-yet-called) Nexus client.
            accountId = ZentrumhubBuildConfig.ACCOUNT_ID,
            apiKey = ZentrumhubBuildConfig.API_KEY,
            channelId = ZentrumhubBuildConfig.CHANNEL_ID,
            // TODO(wholesale): resolve the real device/public IP before production; hardcoded for now.
            customerIp = "114.10.153.62",
            logRequests = isDebugBuild(),
        )
    }
    single { HttpClientFactory(get(), get(), get()) }
    // Zentrumhub clients: public autosuggest + authenticated Nexus (scaffold, wired per Nexus spec).
    single<HttpClient>(named(ZENTRUMHUB_AUTOSUGGEST_CLIENT)) { get<HttpClientFactory>().createAutosuggestClient() }
    single<HttpClient>(named(ZENTRUMHUB_NEXUS_CLIENT)) { get<HttpClientFactory>().createNexusClient() }
}

val appModules: List<Module> = listOf(
    coreModule,
    storageModule,
    networkModule,
    authModule,
    bookingsModule,
    searchModule,
    listingModule,
    wholesaleModule,
    favoritesModule,
)
