package dev.moetz.darksouls

import com.github.twitch4j.TwitchClientBuilder
import dev.moetz.darksouls.data.ChangeLogger
import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.data.DataSource
import dev.moetz.darksouls.data.DivManager
import dev.moetz.darksouls.plugins.configure
import dev.moetz.darksouls.plugins.configureAdmin
import dev.moetz.darksouls.plugins.configureStatic
import dev.moetz.darksouls.plugins.configureWebSocket
import dev.moetz.darksouls.twitch.TwitchChatParser
import feign.Logger
import io.github.bucket4j.Bandwidth
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.Duration

fun main() {

//    val dataFilePath = "./data" //uncomment for development purposes
    val dataFilePath = "/var/dark-souls-backend/data"

    val contentFile = File(dataFilePath, "content.txt")
    val colorFile = File(dataFilePath, "color.txt")
    val logFile = File(dataFilePath, "change.txt")

    val domain = System.getenv("DOMAIN")?.takeIf { it.isNotBlank() } ?: "localhost:8080"
    val isSecure = System.getenv("IS_SECURE")?.takeIf { it.isNotBlank() }?.toBooleanStrict() ?: false

    val adminUserName = System.getenv("ADMIN_USER")?.takeIf { it.isNotBlank() } ?: "admin"
    val adminPassword = System.getenv("ADMIN_PASSWORD")?.takeIf { it.isNotBlank() } ?: "password"

    val twitchCommandChannel = System.getenv("TWITCH_COMMAND_CHANNEL")?.takeIf { it.isNotBlank() }
    val twitchCommandPrefix = System.getenv("TWITCH_COMMAND_PREFIX")?.takeIf { it.isNotBlank() } ?: "!overlay"

    val dataSource = DataSource(contentFile = contentFile, colorFile = colorFile)
    val changeLogger = ChangeLogger(logFile = logFile)
    val divManager = DivManager("/static/font/EBGaramond.ttf")

    val dataManager = DataManager(
        dataSource = dataSource,
        changeLogger = changeLogger
    )

    if (twitchCommandChannel != null) {
        val twitchClient = TwitchClientBuilder.builder()
            .withEnableChat(true)
            .withChatJoinLimit(Bandwidth.simple(10, Duration.ofSeconds(11)))
            .withFeignLogLevel(Logger.Level.NONE)
            .build()

        val parser = TwitchChatParser(
            twitchClient = twitchClient,
            messagePrefix = twitchCommandPrefix,
            channelName = twitchCommandChannel
        )
        parser.startChatObservingThread { commandPayload ->
            runBlocking {
                dataManager.update(content = commandPayload, color = null)
            }
        }
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
        }
        install(DefaultHeaders)
        install(AutoHeadResponse)
        install(CachingHeaders) {
            options {
                CachingOptions(CacheControl.NoCache(CacheControl.Visibility.Public))
            }
        }
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
        }

        configure(dataManager, divManager, domain, isSecure)
        configureStatic()
        configureAdmin(dataManager, adminUserName, adminPassword)
        configureWebSocket(dataManager, divManager)
    }.start(wait = true)
}
