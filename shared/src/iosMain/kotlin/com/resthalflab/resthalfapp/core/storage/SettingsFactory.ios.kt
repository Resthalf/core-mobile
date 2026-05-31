package com.resthalflab.resthalfapp.core.storage

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

actual fun defaultSettingsFactory(): SettingsFactory = SettingsFactory { name ->
    NSUserDefaultsSettings(NSUserDefaults(suiteName = name) ?: NSUserDefaults.standardUserDefaults)
}
