package com.resthalflab.resthalfapp.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
)

@Serializable
data class GuestDto(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val idNumber: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class StaffDto(
    val id: String,
    val name: String,
    val phone: String,
    val role: String,
    val hotelId: String? = null,
)

@Serializable
data class GuestAuthResponse(
    val guest: GuestDto,
    val token: String,
)

@Serializable
data class StaffAuthResponse(
    val staff: StaffDto,
    val token: String,
)
