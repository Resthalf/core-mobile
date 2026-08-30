package com.resthalflab.resthalfapp.feature.favorites

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.feature.favorites.api.FavoritesRepository
import com.resthalflab.resthalfapp.feature.favorites.data.SettingsFavoritesRepository
import com.resthalflab.resthalfapp.feature.favorites.ui.DefaultFavoritesComponent
import com.resthalflab.resthalfapp.feature.favorites.ui.FavoritesComponent
import com.resthalflab.resthalfapp.feature.search.api.HotelDetailArgs
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val favoritesModule: Module = module {
    single<FavoritesRepository> {
        SettingsFavoritesRepository(get<SettingsFactory>().create("resthalf.favorites"))
    }
}

fun favoritesComponent(
    componentContext: ComponentContext,
    koin: Koin,
    onOpenHotelDetail: (HotelDetailArgs) -> Unit,
): FavoritesComponent =
    DefaultFavoritesComponent(componentContext, koin.get(), koin.get(), onOpenHotelDetail)
