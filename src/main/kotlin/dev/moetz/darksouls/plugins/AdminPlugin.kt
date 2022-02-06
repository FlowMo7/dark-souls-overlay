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
                        title("Dark Souls Overlay")

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
