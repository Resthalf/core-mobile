package com.resthalflab.resthalfapp.feature.favorites.ui

import com.arkivanov.decompose.ComponentContext

interface FavoritesComponent

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
) : FavoritesComponent, ComponentContext by componentContext
