package com.aethink

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
)