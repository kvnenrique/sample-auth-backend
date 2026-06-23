package com.aethink.routes

import com.aethink.data.UserRepositoryInMemory
import com.aethink.domain.User
import com.aethink.dto.LoginRequest
import com.aethink.dto.LoginResponse
import com.aethink.dto.RegisterRequest
import com.aethink.dto.UserResponse
import com.aethink.extensions.seconds
import com.aethink.security.BCryptPasswordHasher
import com.aethink.security.JwtConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import java.util.Date

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
            val expiresIn = JwtConfig.accessTokenExpiresIn // seconds

            val accessToken = JWT.create()
                .withAudience(JwtConfig.audience)
                .withIssuer(JwtConfig.issuer)
                .withClaim(JwtConfig.emailClaim, existingUser.email)
                .withExpiresAt(Date(System.currentTimeMillis() + expiresIn.seconds))
                .sign(Algorithm.HMAC256(JwtConfig.secret))

            val response = LoginResponse(
                accessToken,
                "Bearer",
                expiresIn
            )


            call.respond(
                HttpStatusCode.OK,
                response
            )
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