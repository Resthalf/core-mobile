package com.resthalflab.resthalfapp.feature.profile

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.profile.ui.DefaultProfileComponent
import com.resthalflab.resthalfapp.feature.profile.ui.ProfileComponent
import org.koin.core.Koin

/** Feature entry point. Uses AuthApi from DI for logout. */
fun profileComponent(componentContext: ComponentContext, koin: Koin): ProfileComponent =
    DefaultProfileComponent(componentContext, koin.get())
