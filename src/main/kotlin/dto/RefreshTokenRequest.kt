package com.aethink.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)