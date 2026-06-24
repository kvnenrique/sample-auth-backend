package com.aethink.security

object JwtConfig {
    const val secret = "some-long-secret"
    const val issuer = "sample authentication backend"
    const val audience = "sample authentication backend clients"
    const val realm = "sample authentication backend"
    const val emailClaim = "email"
    const val accessTokenExpiresIn = 3_600L
}