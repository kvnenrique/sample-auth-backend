package com.aethink

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        get("/articles") {
            // Get all articles ...
            val sort = call.request.queryParameters["sort"] ?: "new"
            call.respond("List of articles sorted starting from $sort")
        }

        get("/articles/{articleId}") {
            val articleId = call.parameters["articleId"]
            call.respond("Article details for $articleId")
        }
    }
}
