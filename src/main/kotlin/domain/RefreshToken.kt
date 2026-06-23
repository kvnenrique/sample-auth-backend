package com.aethink.domain

import kotlin.time.Instant

class RefreshToken(
    val id: String,
    val user_id: String,
    val token_hash: String,
    val expires_at: Instant,
    val revoked_at: Instant,
    val created_at: Instant,
    val replaced_by_token_id: String
)