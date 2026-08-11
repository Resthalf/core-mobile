package com.resthalflab.resthalfapp.core.storage

import com.russhwolf.settings.Settings

// Phase 1 stub. Replace with Keychain (iOS) + EncryptedSharedPreferences (Android) in Phase 3.
class SettingsSecureTokenStore(
    private val settings: Settings,
) : SecureTokenStore {

    override fun read(): AuthTokens? {
        val access = settings.getStringOrNull(KEY_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        return AuthTokens(accessToken = access, refreshToken = refresh)
    }

    override fun save(tokens: AuthTokens) {
        settings.putString(KEY_ACCESS, tokens.accessToken)
        settings.putString(KEY_REFRESH, tokens.refreshToken)
    }

    override fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access"
        const val KEY_REFRESH = "auth.refresh"
    }
}
