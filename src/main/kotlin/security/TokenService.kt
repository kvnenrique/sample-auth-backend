package com.aethink.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

object TokenService {
    private val secureRandom = SecureRandom()

    fun createAccessToken(email: String): String {
        val expiresInMillis: Long = JwtConfig.accessTokenExpiresIn
        return JWT.create()
            .withAudience(JwtConfig.audience)
            .withIssuer(JwtConfig.issuer)
            .withClaim(JwtConfig.emailClaim, email)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMillis))
            .sign(Algorithm.HMAC256(JwtConfig.secret))
    }

    fun generateRefreshToken(): String {
        val randomBytes = ByteArray(64)
        secureRandom.nextBytes(randomBytes)

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes)
    }

    fun hashRefreshToken(refreshToken: String): String {
        val messageDigestAlgorithm = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigestAlgorithm.digest(refreshToken.toByteArray())

        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun getRefreshTokenExpirationInstant(): Instant {
        return Clock.System.now().plus(30.days)
    }
}