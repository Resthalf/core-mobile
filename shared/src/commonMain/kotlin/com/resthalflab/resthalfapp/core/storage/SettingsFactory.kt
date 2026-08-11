package com.resthalflab.resthalfapp.core.storage

import com.russhwolf.settings.Settings

fun interface SettingsFactory {
    fun create(name: String): Settings
}

expect fun defaultSettingsFactory(): SettingsFactory
