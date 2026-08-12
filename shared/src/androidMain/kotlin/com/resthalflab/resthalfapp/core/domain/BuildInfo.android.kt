package com.resthalflab.resthalfapp.core.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.mp.KoinPlatform

// Reads the app's debuggable flag rather than a generated BuildConfig, so the shared library needs
// no BuildConfig plumbing. Context comes from Koin (mirrors SettingsFactory.android.kt).
actual fun isDebugBuild(): Boolean {
    val context = KoinPlatform.getKoin().get<Context>()
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
