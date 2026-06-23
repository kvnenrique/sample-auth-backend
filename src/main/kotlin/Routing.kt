package com.aethink

import com.aethink.routes.authRoutes
import com.aethink.routes.meRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        /*
         * Home
         * */
        get("/") {
            call.respondText("ÆThink - Sample Authentication Backend")
        }

        /*
         * Auth routes
         * */
        authRoutes()

        /*
         * /me
         * */
        meRoutes()
    }
}
