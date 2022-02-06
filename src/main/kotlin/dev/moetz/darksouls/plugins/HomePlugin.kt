package dev.moetz.darksouls.plugins

import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.data.DivManager
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import kotlinx.serialization.Serializable

@Serializable
data class ApiContent(
    val content: String,
    val color: String?
)

fun Application.configure(
    dataManager: DataManager,
    divManager: DivManager,
    publicHostname: String,
    isSecure: Boolean
) {

    routing {

        get {
            val dataUpdate = dataManager.getData()

            call.respondHtml {
                head {
                    meta(charset = "utf-8")
                    title("Dark Souls Counter")
                    script(
                        type = "text/javascript",
                        src = "/static/reconnecting-websocket.min.js"
                    ) {

                    }
                    script(
                        type = "text/javascript",
                        src = "/static/script.js"
                    ) {

                    }

                    script(type = "text/javascript") {
                        unsafe { +"window.onload = function() { initWebsocket('${if (isSecure) "wss" else "ws"}://${publicHostname}/ws/content'); };" }
                    }
                }
                body {
                    div {
                        id = "loaded_content"
                        style = "font-size: 4em;"
                        unsafe {
                            +divManager.dataUpdateToHtmlDiv(dataUpdate)
                        }
                    }
                }
            }
        }

        get("content") {
            call.respondText(contentType = ContentType.Text.Html) {
                val dataUpdate = dataManager.getData()
                divManager.dataUpdateToHtmlDiv(dataUpdate)
            }
        }

        route("api") {
            get("content") {
                val dataUpdate = dataManager.getData()
                val responseBody = ApiContent(
                    content = dataUpdate.content,
                    color = dataUpdate.color
                )
                call.respond(responseBody)
            }
        }
    }
}
