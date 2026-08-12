package com.resthalflab.resthalfapp.feature.wholesale

import com.resthalflab.resthalfapp.core.network.ZENTRUMHUB_AUTOSUGGEST_CLIENT
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.data.DefaultLocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.data.LocationRemote
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val wholesaleModule: Module = module {
    single { LocationRemote(get(named(ZENTRUMHUB_AUTOSUGGEST_CLIENT))) }
    single<LocationSearchApi> { DefaultLocationSearchApi(get()) }
}
