package com.resthalflab.resthalfapp.feature.wholesale

import com.resthalflab.resthalfapp.core.network.ZENTRUMHUB_AUTOSUGGEST_CLIENT
import com.resthalflab.resthalfapp.core.network.ZENTRUMHUB_NEXUS_CLIENT
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleBookingApi
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleDetailApi
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.data.DefaultLocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.data.DefaultWholesaleBookingApi
import com.resthalflab.resthalfapp.feature.wholesale.data.DefaultWholesaleDetailApi
import com.resthalflab.resthalfapp.feature.wholesale.data.DefaultWholesaleSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.data.LocationRemote
import com.resthalflab.resthalfapp.feature.wholesale.data.NexusRemote
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val wholesaleModule: Module = module {
    single { LocationRemote(get(named(ZENTRUMHUB_AUTOSUGGEST_CLIENT))) }
    single { NexusRemote(get(named(ZENTRUMHUB_NEXUS_CLIENT))) }
    single<LocationSearchApi> { DefaultLocationSearchApi(get()) }
    single<WholesaleSearchApi> { DefaultWholesaleSearchApi(get(), get(), get()) }
    single<WholesaleDetailApi> { DefaultWholesaleDetailApi(get(), get()) }
    single<WholesaleBookingApi> { DefaultWholesaleBookingApi(get()) }
}
