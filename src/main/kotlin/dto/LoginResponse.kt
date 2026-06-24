package com.aethink.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long, // seconds
    val refreshToken: String
)