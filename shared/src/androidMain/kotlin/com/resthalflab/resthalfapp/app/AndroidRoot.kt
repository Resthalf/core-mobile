package com.resthalflab.resthalfapp.app

import android.content.Context
import androidx.activity.ComponentActivity
import com.arkivanov.decompose.defaultComponentContext
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun initKoin(appContext: Context) {
    if (GlobalContext.getOrNull() != null) return
    startKoin {
        androidContext(appContext)
        modules(appModules)
    }
}

fun ComponentActivity.createRootComponent(): RootComponent {
    val koin = GlobalContext.get()
    return DefaultRootComponent(
        componentContext = defaultComponentContext(),
        auth = koin.get<AuthApi>(),
        koin = koin,
    )
}
