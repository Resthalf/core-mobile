package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import com.resthalflab.resthalfapp.feature.auth.data.dto.GuestAuthResponse
import com.resthalflab.resthalfapp.feature.auth.data.dto.LoginRequest
import com.resthalflab.resthalfapp.feature.auth.data.dto.RegisterRequest
import com.resthalflab.resthalfapp.feature.auth.data.dto.StaffAuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Common shape both guest and staff responses map to. */
data class AuthResult(val token: String, val session: AuthSession)

/** Uses the bare auth client (no Bearer plugin) — login/register don't need a token. */
class AuthRemote(
    private val client: HttpClient,
) {
    suspend fun login(accountType: AccountType, phone: String, password: String): AuthResult {
        val body = LoginRequest(phone = phone, password = password)
        return when (accountType) {
            AccountType.Guest ->
                client.post("auth/guest/login") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }.body<GuestAuthResponse>().toResult()

            AccountType.Staff ->
                client.post("auth/staff/login") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }.body<StaffAuthResponse>().toResult()
        }
    }

    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AuthResult =
        client.post("auth/guest/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(fullName = fullName, phone = phone, email = email, password = password))
        }.body<GuestAuthResponse>().toResult()
}

private fun GuestAuthResponse.toResult(): AuthResult = AuthResult(
    token = token,
    session = AuthSession(
        id = guest.id,
        displayName = guest.fullName,
        phone = guest.phone,
        accountType = AccountType.Guest,
        email = guest.email,
    ),
)

private fun StaffAuthResponse.toResult(): AuthResult = AuthResult(
    token = token,
    session = AuthSession(
        id = staff.id,
        displayName = staff.name,
        phone = staff.phone,
        accountType = AccountType.Staff,
        role = staff.role,
        hotelId = staff.hotelId,
    ),
)
