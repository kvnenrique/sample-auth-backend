package com.aethink.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequest(
    val refreshToken: String
)