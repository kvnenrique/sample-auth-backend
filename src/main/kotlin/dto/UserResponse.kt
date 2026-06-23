package com.aethink.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val name: String,
    val email: String
)