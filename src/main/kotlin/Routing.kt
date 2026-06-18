package com.aethink

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.request
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.Date

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("ÆThink - Sample Authentication Backend")
        }

        post("/auth/register") {
            val requestBody = call.receive<RegisterUserRequest>()

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

        post("/auth/login") {
            val requestBody = call.receive<LoginRequest>()

            val email = requestBody.email
            val password = requestBody.password

            if (email.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "email should not be blank"
                )
                return@post
            }

            val existingUser = UserRepositoryInMemory.findUserByEmail(email)
            if (existingUser == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "User does not exist"
                )
                return@post
            }

            if (!BCryptPasswordHasher.verify(password, existingUser.passwordHash)) {
                // Wrong password
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Wrong password"
                )
            }

            /**
             * If login success
             */
            val jwtSecret = "some-long-secret" // Don't hardcode the secret on real projects, put them on .env

            val accessToken = JWT.create()
                .withAudience("sample authentication backend clients")
                .withIssuer("sample authentication backend")
                .withClaim("email", existingUser.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .sign(Algorithm.HMAC256(jwtSecret))

            val response = LoginResponse(
                accessToken,
                "Bearer",
                3600
            )


            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        get("/me") {
            call.respond(
                HttpStatusCode.OK,
                "Me"
            )
        }
    }
}
