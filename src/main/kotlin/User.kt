package com.aethink

data class User(
    val id: Int,
    val userName: String,
    val email: String,
    val passwordHash: String
)