package com.aethink

import io.ktor.client.request.request
import io.ktor.client.utils.HttpRequestIsReadyForSending
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

            val passwordHash = "SamplePasswordHash"
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
            call.respond(
                HttpStatusCode.OK,
                "Login"
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
