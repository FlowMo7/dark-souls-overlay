package dev.moetz.darksouls.plugins

import dev.moetz.darksouls.data.ChangeLogger
import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.util.OffsetDateTimeSerializer
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@kotlinx.serialization.Serializable
data class PostContentBody(
    val content: String,
    val color: String
)

@kotlinx.serialization.Serializable
data class LogResponse(
    val changes: List<Change>
) {
    @kotlinx.serialization.Serializable
    data class Change(
        @Serializable(OffsetDateTimeSerializer::class)
        val dateTime: OffsetDateTime,
        val text: String
    )
}

fun Application.configureAdmin(
    dataManager: DataManager,
    changeLogger: ChangeLogger,
    adminUserName: String,
    adminUserPassword: String,
    isSecure: Boolean,
    publicHostname: String
) {

    authentication {
        basic(name = "basic-auth") {
            realm = "Counter Admin Realm"
            validate { credentials ->
                if (credentials.name == adminUserName && credentials.password == adminUserPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    routing {

        authenticate("basic-auth") {

            route("static") {
                resource(remotePath = "admin-script.js", resource = "admin-script.js")
            }

            route("api/admin") {
                post("content") {
                    try {
                        val requestBody = call.receive<PostContentBody>()
                        dataManager.update(requestBody.content, requestBody.color)
                        call.respond(HttpStatusCode.OK, "")
                    } catch (throwable: Throwable) {
                        call.respond(status = HttpStatusCode.BadRequest, "Bad request: $throwable")
                    }
                }

                webSocket("ws/log") {
                    changeLogger.getLogHtmlWebsocketFlow()
                        .onEach { list ->
                            send(
                                Frame.Text(
                                    Json.encodeToString(
                                        LogResponse.serializer(),
                                        LogResponse(
                                            changes = list.map { (dateTime, text) ->
                                                LogResponse.Change(
                                                    dateTime = dateTime,
                                                    text = text
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        }
                        .flowOn(Dispatchers.IO)
                        .launchIn(this)

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                println("received text-frame: $receivedText")
                            }
                        }
                    }
                }
            }

            get("set/") {
                call.respondRedirect("/set", permanent = false)
            }

            get("set") {
                val dataUpdate = dataManager.getData()
                call.respondHtml {
                    head {
                        meta(charset = "utf-8")
                        title("Dark Souls Overlay")

                        script(
                            type = "text/javascript",
                            src = "/static/reconnecting-websocket.min.js"
                        ) {

                        }
                        script(
                            type = "text/javascript",
                            src = "/static/admin-script.js"
                        ) {

                        }

                        script(type = "text/javascript") {
                            unsafe { +"window.onload = function() { initLogWebsocket('${if (isSecure) "wss" else "ws"}://${publicHostname}/api/admin/ws/log'); };" }
                        }

                        style {
                            unsafe {
                                +"body {"
                                +"font-size: 1em;"
                                +"}"

                                +".inputfield {"
                                +"border: 1px solid #B8B8B8;"
                                +"padding: 0.8em 0.8em;"
                                +"border-radius: 5px;"
                                +"font-size: 1em;"
                                +"-webkit-box-sizing: border-box;"
                                +"-moz-box-sizing: border-box;"
                                +"box-sizing: border-box;"
                                +"}"

                                +".inputbutton {"
                                +"color: #FFFFFF;"
                                +"background-color: orange;"
                                +"cursor: pointer;"
                                +"border: 0;"
                                +"border-radius: 5px;"
                                +"padding: 0.8em 1.2em;"
                                +"font-size: 1em;"
                                +"}"
                            }
                        }
                    }
                    body {
                        h2 { +"Dark Souls Overlay" }
                        div {
                            p {
                                +"The text entered here will be displayed in the stream once submitted by clicking 'Submit'."
                                br()
                                +"'\\n' can be used to create a line-break."
                            }
                            form(action = "/set", method = FormMethod.post) {
                                table {
                                    tr {
                                        td {
                                            +"Content to Display:"
                                        }
                                        td {
                                            textInput(classes = "inputfield", name = "content") {
                                                value = dataUpdate.content.escapeHTML()
                                            }
                                        }
                                    }


                                    tr {
                                        td { +"Font-Color: " }
                                        td {
                                            textInput(classes = "inputfield", name = "color") {
                                                value = dataUpdate.color.escapeHTML()
                                            }
                                        }
                                        td {
                                            submitInput(classes = "inputbutton")
                                        }
                                    }
                                    tr {
                                        td {}
                                        td {
                                            i {
                                                +"Only "
                                                a(
                                                    href = "https://htmlcolorcodes.com/",
                                                    target = "_blank"
                                                ) { +"6-digit HEX color codes " }
                                                +" are allowed."
                                            }
                                        }
                                    }
                                }
                            }

                            br()
                            br()
                            b { +"Changelog (last 10 entries):" }
                            pre {
                                div {
                                    id = "log_content"
                                }
                            }
                        }
                    }
                }
            }

            post("set") {
                val (contentParameter, colorParameter) = try {
                    val parameters = call.receiveParameters()
                    parameters["content"] to parameters["color"]
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    null to null
                }
                if (contentParameter != null && colorParameter != null) {
                    try {
                        dataManager.update(contentParameter, colorParameter)
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }
                }
                call.respondRedirect("/set", permanent = false)
            }
        }
    }
}
