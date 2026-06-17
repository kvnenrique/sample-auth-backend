package com.aethink

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
            // register user
            call.respond(
                HttpStatusCode.OK,
                "Register"
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
