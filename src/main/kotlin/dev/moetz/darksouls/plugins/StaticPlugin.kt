package dev.moetz.darksouls.plugins

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureStatic() {
    routing {

        route("static") {

            resource(remotePath = "reconnecting-websocket.js", resource = "reconnecting-websocket.js")
            resource(remotePath = "reconnecting-websocket.min.js", resource = "reconnecting-websocket.min.js")

            resource(remotePath = "script.js", resource = "script.js")

            route("font") {
                resource(remotePath = "EBGaramond.ttf", resource = "EBGaramond.ttf")
            }

        }

    }
}
