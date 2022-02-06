package dev.moetz.darksouls.plugins

import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.data.DivManager
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


fun Application.configureWebSocket(dataManager: DataManager, divManager: DivManager) {
    routing {
        route("ws") {
            webSocket("content") {
                dataManager.dataUpdateFlow
                    .onEach { dataUpdate ->
                        println("received a data update from dataSource: $dataUpdate")
                        send(Frame.Text(divManager.dataUpdateToHtmlDiv(dataUpdate)))
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

    }
}
