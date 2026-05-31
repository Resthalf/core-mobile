package com.resthalflab.resthalfapp.core.storage

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.mp.KoinPlatform

actual fun defaultSettingsFactory(): SettingsFactory = SettingsFactory { name ->
    val context = KoinPlatform.getKoin().get<Context>()
    SharedPreferencesSettings(context.getSharedPreferences(name, Context.MODE_PRIVATE))
}
