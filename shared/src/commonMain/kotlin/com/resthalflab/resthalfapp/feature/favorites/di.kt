package com.resthalflab.resthalfapp.feature.favorites

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.favorites.ui.DefaultFavoritesComponent
import com.resthalflab.resthalfapp.feature.favorites.ui.FavoritesComponent

/** Feature entry point. Placeholder until favorites data lands. */
fun favoritesComponent(componentContext: ComponentContext): FavoritesComponent =
    DefaultFavoritesComponent(componentContext)
