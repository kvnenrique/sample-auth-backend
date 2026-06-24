package com.aethink.routes

import com.aethink.data.RefreshTokenRepositoryI
import com.aethink.data.RefreshTokenRepositoryInMemory
import com.aethink.data.UserRepositoryInMemory
import com.aethink.domain.RefreshToken
import com.aethink.domain.User
import com.aethink.dto.LoginRequest
import com.aethink.dto.LoginResponse
import com.aethink.dto.RefreshTokenRequest
import com.aethink.dto.RegisterRequest
import com.aethink.dto.UserResponse
import com.aethink.security.BCryptPasswordHasher
import com.aethink.security.JwtConfig
import com.aethink.security.TokenService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import kotlin.time.Clock

fun Route.authRoutes() {
    route("/auth") {
        /*
         * POST /auth/register
         * */
        post("/register") {
            val requestBody = call.receive<RegisterRequest>()

            val email = requestBody.email;
            val password = requestBody.password;

            if (email.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Email can not be blank"
                )
                return@post
            }

            if (password.length < 8) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Password must be at least 8 characters long"
                )
                return@post
            }

            if (UserRepositoryInMemory.findUserByEmail(email) != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    "User already exists"
                )
                return@post
            }

            val passwordHash = BCryptPasswordHasher.hash(password)
            val newUser = User(
                requestBody.name,
                requestBody.email,
                passwordHash
            )

            UserRepositoryInMemory.createUser(newUser)

            val response = UserResponse(
                newUser.name,
                newUser.email
            )

            // register user
            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        /*
         * POST /auth/login
         * */
        post("/login") {
            val requestBody = call.receive<LoginRequest>()

            val email = requestBody.email
            val password = requestBody.password

            if (email.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "email can not be blank"
                )
                return@post
            }

            val existingUser = UserRepositoryInMemory.findUserByEmail(email)
            if (existingUser == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Bad credentials"
                )
                return@post
            }

            if (!BCryptPasswordHasher.verify(password, existingUser.passwordHash)) {
                // Wrong password
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Bad credentials"
                )
                return@post
            }

            /**
             * If login success
             */
            // Access token
            val accessToken = TokenService.createAccessToken(email)
            val expiresIn = JwtConfig.accessTokenExpiresIn // seconds
            // Refresh token
            val createdAt = Clock.System.now()
            val rawRefreshToken = TokenService.generateRefreshToken()
            val refreshTokenHash = TokenService.hashRefreshToken(rawRefreshToken)
            val refreshToken = RefreshToken(
                UUID.randomUUID().toString(),
                existingUser.email,
                refreshTokenHash,
                createdAt,
                TokenService.getRefreshTokenExpirationInstant(createdAt),
                null,
                null
            )

            // Save refresh token
            RefreshTokenRepositoryInMemory.saveRefreshToken(refreshToken)

            // Respond both acces and refresh token
            val loginResponse = LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                rawRefreshToken
            )

            call.respond(
                HttpStatusCode.OK,
                loginResponse
            )
        }

        /*
         * POST /auth/refresh
         * */
        post("/refresh") {
            val requestBody = call.receive<RefreshTokenRequest>()
            val rawRefreshToken = requestBody.refreshToken

            if (rawRefreshToken.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Refresh token can not be blank"
                )
                return@post
            }

            val refreshTokenHash = TokenService.hashRefreshToken(rawRefreshToken)
            val existingRefreshToken = RefreshTokenRepositoryInMemory
                .findRefreshTokenByTokenHash(refreshTokenHash)

            // Check token exists
            if (existingRefreshToken == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid refresh token"
                )
                return@post
            }

            // Check token hasn't expired
            val now = Clock.System.now()
            if (existingRefreshToken.expires_at <= now) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid refresh token"
                )
                return@post
            }

            // Check token hasn't been revoked
            if (existingRefreshToken.revoked_at != null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid refresh token"
                )
                return@post
            }

            val existingUser = UserRepositoryInMemory.findUserByEmail(
                existingRefreshToken.user_id
            )

            if (existingUser == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid refresh token"
                )
                return@post
            }

            // Create a new access and refresh tokens
            val newAccessToken = TokenService.createAccessToken(existingUser.email)

            val newRefreshTokenCreatedAt = Clock.System.now()
            val newRawRefreshToken = TokenService.generateRefreshToken()
            val newRefreshTokenHash = TokenService.hashRefreshToken(newRawRefreshToken)

            val newRefreshToken = RefreshToken(
                UUID.randomUUID().toString(),
                existingUser.email,
                newRefreshTokenHash,
                newRefreshTokenCreatedAt,
                TokenService.getRefreshTokenExpirationInstant(newRefreshTokenCreatedAt),
                null,
                null
            )

            // Save new refresh token and revoke old one
            RefreshTokenRepositoryInMemory.saveRefreshToken(newRefreshToken)

            RefreshTokenRepositoryInMemory.revokeRefreshTokenById(
                existingRefreshToken.id,
                newRefreshToken.id
            )

            val loginResponse = LoginResponse(
                newAccessToken,
                "Bearer",
                JwtConfig.accessTokenExpiresIn,
                newRawRefreshToken
            )

            call.respond(
                HttpStatusCode.OK,
                loginResponse
            )
        }

        /*
         * POST /auth/logout
         * */
        post("/logout") {
            val requestBody = call.receive<RefreshTokenRequest>()
            val rawRefreshToken = requestBody.refreshToken

            if (rawRefreshToken.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Refresh token can not be blank"
                )
                return@post
            }

            val refreshTokenHash = TokenService.hashRefreshToken(rawRefreshToken)

            val existingRefreshToken = RefreshTokenRepositoryInMemory
                .findRefreshTokenByTokenHash(refreshTokenHash)

            if (existingRefreshToken != null) {
                RefreshTokenRepositoryInMemory.revokeRefreshTokenById(
                    existingRefreshToken.id
                )
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}

fun Route.meRoutes() {
    authenticate("auth-jwt") {
        get("/me") {
            val principal = call.principal<JWTPrincipal>()
            val email = principal?.payload?.getClaim(JwtConfig.emailClaim)?.asString()

            if (email.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid token"
                )
                return@get
            }

            val user = UserRepositoryInMemory.findUserByEmail(email)
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Invalid token"
                )
                return@get
            }

            val response = UserResponse(
                user.name,
                user.email
            )

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }
    }
}