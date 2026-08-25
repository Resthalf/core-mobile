package com.resthalflab.resthalfapp.feature.auth.data

import com.benasher44.uuid.uuid4
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import com.resthalflab.resthalfapp.feature.auth.api.GoogleAccount
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Local-first auth: the session lives entirely on-device (no own backend). Google sign-in maps a
 * Firebase account into an [AuthSession]; guest creates an anonymous one. The phone/password
 * [login]/[register] paths are intentionally inert here (kept on [AuthApi] for a future backend).
 */
class LocalAuthRepository(
    settingsFactory: SettingsFactory,
) : AuthApi {

    private val settings: Settings = settingsFactory.create("resthalf.auth")
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(load())
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override suspend fun signInWithGoogle(account: GoogleAccount) {
        persist(
            AuthSession(
                id = account.uid,
                displayName = account.displayName?.takeIf { it.isNotBlank() }
                    ?: account.email?.substringBefore('@')
                    ?: "Traveler",
                phone = "",
                accountType = AccountType.Guest,
                email = account.email,
                photoUrl = account.photoUrl,
            )
        )
    }

    override fun continueAsGuest() {
        persist(
            AuthSession(
                id = "guest-${uuid4()}",
                displayName = "Guest",
                phone = "",
                accountType = AccountType.Guest,
            )
        )
    }

    override suspend fun login(accountType: AccountType, phone: String, password: String): AppResult<Unit> =
        AppResult.Failure(AppError.Unknown("Phone sign-in isn't available. Continue with Google."))

    override suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AppResult<Unit> =
        AppResult.Failure(AppError.Unknown("Sign up isn't available. Continue with Google."))

    override suspend fun logout() {
        signOutPlatformAuth()
        settings.remove(KEY_SESSION)
        _session.value = null
    }

    private fun persist(session: AuthSession) {
        settings.putString(KEY_SESSION, json.encodeToString(session))
        _session.value = session
    }

    private fun load(): AuthSession? =
        settings.getStringOrNull(KEY_SESSION)
            ?.let { runCatching { json.decodeFromString<AuthSession>(it) }.getOrNull() }

    private companion object {
        const val KEY_SESSION = "auth.session"
    }
}
