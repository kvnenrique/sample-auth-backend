package com.aethink.domain

import kotlin.time.Instant

data class RefreshToken(
    val id: String,
    val user_id: String,
    val token_hash: String,
    val created_at: Instant,
    val expires_at: Instant,
    val revoked_at: Instant? = null,
    val replaced_by_token_id: String? = null
)
