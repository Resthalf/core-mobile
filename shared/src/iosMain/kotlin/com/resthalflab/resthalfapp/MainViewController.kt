package com.resthalflab.resthalfapp

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.resthalflab.resthalfapp.app.DefaultRootComponent
import com.resthalflab.resthalfapp.app.appModules
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val koin = initKoinIfNeeded()

    val lifecycle = LifecycleRegistry()
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        auth = koin.get<AuthApi>(),
        koin = koin,
    )
    lifecycle.resume()

    return ComposeUIViewController { App(root) }
}

private fun initKoinIfNeeded(): org.koin.core.Koin {
    val existing = GlobalContext.getOrNull()
    if (existing != null) return existing
    return startKoin { modules(appModules) }.koin
}
