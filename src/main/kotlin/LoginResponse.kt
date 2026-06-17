package com.aethink

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
)