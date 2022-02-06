package dev.moetz.darksouls.plugins

import dev.moetz.darksouls.data.DataManager
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.html.*

data class PostContentBody(
    val content: String,
    val color: String
)

fun Application.configureAdmin(
    dataManager: DataManager,
    adminUserName: String,
    adminUserPassword: String
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
            }

            get("set/") {
                call.respondRedirect("/set", permanent = false)
            }

            get("set") {
                val dataUpdate = dataManager.getData()
                call.respondHtml {
                    head {
                        meta(charset = "utf-8")
                        title("Set the Dark Souls Counter Text")
                    }
                    body {
                        div {
                            p {
                                +"This is the console for the dark souls stream overlay."
                                br()
                                +"The Text entered here will be displayed in the stream once submitted by clicking 'Submit'."
                                br()
                                +"'\\n' can be used to create a line-break."
                            }
                            form(action = "/set", method = FormMethod.post) {
                                textInput(name = "content") {
                                    value = dataUpdate.content.escapeHTML()
                                }
                                submitInput()

                                br()
                                br()
                                br()

                                +"Additionally, the font-color can be adjusted here."
                                br()
                                +"Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well."
                                br()
                                +"#"
                                textInput(name = "color") {
                                    val color = dataUpdate.color ?: "FFFFFF"
                                    value = color.escapeHTML()
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
