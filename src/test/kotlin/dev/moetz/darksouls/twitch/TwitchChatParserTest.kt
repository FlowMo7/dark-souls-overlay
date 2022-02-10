package dev.moetz.darksouls.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.IRCMessageEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TwitchChatParserTest {

    private lateinit var twitchChatParser: TwitchChatParser
    private lateinit var twitchClient: TwitchClient

    @Before
    fun setUp() {
        twitchClient = mockk()

        twitchChatParser = TwitchChatParser(
            twitchClient = twitchClient,
            messagePrefix = "!overlay",
            channelName = "TestChannel"
        )
    }

    private fun generateIRCMessageEvent(
        subscriberMonths: Int,
        isModerator: Boolean = false,
        isBroadcaster: Boolean = false,
        isVip: Boolean = false,
        color: String = "#FF4500;",
        senderName: String,
        channelName: String,
        message: String
    ): IRCMessageEvent {
        val rawMessage = buildString {
            append("@badge-info=")
            if (subscriberMonths > 0) {
                append("subscriber/$subscriberMonths")
            }
            append(";")
            append("badges=")

            if (isBroadcaster) {
                append("broadcaster/1,")
            }
            if (isModerator) {
                append("moderator/1,")
            }
            if (isVip) {
                append("vip/1,")
            }
            if (subscriberMonths > 0) {
                append("subscriber/$subscriberMonths")
            }
            append(";")

            append("color=$color;")

            append("display-name=$senderName")

            append("emotes=;")
            append("first-msg=0;")
            append("flags=;")
            append("id=9f456b32-1234-4321-1234-e5f4d52faf6d;")
            append("mod=")
            if (isModerator) {
                append('1')
            } else {
                append('0')
            }
            append(";")
            append("room-id=123456789;")
            append("subscriber=${if (subscriberMonths > 0) "1" else "0"};")
            append("tmi-sent-ts=1643304423421;")
            append("turbo=0;")
            append("user-id=123456;")

            append("user-type=")
            if (isModerator) {
                append("mod")
            }

            append(" ")

            append(":${senderName.toLowerCase()}!${senderName.toLowerCase()}@${senderName.toLowerCase()}.tmi.twitch.tv")
            append(" PRIVMSG #${channelName.toLowerCase()} ")
            append(":")
            append(message)
        }
        return IRCMessageEvent(
            rawMessage,
            emptyMap(),
            emptyMap(),
            emptyList()
        )
    }

    @Test
    fun `message does not contain prefix and is not from broadcaster or mod`() {
        val event = generateIRCMessageEvent(
            subscriberMonths = 6,
            isModerator = false,
            isVip = false,
            color = "#FF4500",
            senderName = "MessageSender",
            channelName = "TestChannel",
            message = "This is a message without the command prefix"
        )

        val lambda = mockk<(String) -> Unit>(relaxed = true)

        twitchChatParser.onIRCMessageEvent(event, lambda)

        verify(exactly = 0) { lambda.invoke(any()) }
    }

    @Test
    fun `message contains prefix but is not from broadcaster or mod`() {
        val event = generateIRCMessageEvent(
            subscriberMonths = 6,
            isModerator = false,
            isVip = false,
            color = "#FF4500",
            senderName = "MessageSender",
            channelName = "TestChannel",
            message = "!overlay This is a message with the command prefix"
        )

        val lambda = mockk<(String) -> Unit>(relaxed = true)

        twitchChatParser.onIRCMessageEvent(event, lambda)

        verify(exactly = 0) { lambda.invoke(any()) }
    }

    @Test
    fun `message does not contain prefix but is from mod`() {
        val event = generateIRCMessageEvent(
            subscriberMonths = 6,
            isModerator = true,
            isVip = false,
            color = "#FF4500",
            senderName = "ModMessageSender",
            channelName = "TestChannel",
            message = "This is a message without the command prefix"
        )

        val lambda = mockk<(String) -> Unit>(relaxed = true)

        twitchChatParser.onIRCMessageEvent(event, lambda)

        verify(exactly = 0) { lambda.invoke(any()) }
    }

    @Test
    fun `message contains prefix and is from mod`() {
        val event = generateIRCMessageEvent(
            subscriberMonths = 6,
            isModerator = true,
            isVip = false,
            color = "#FF4500",
            senderName = "ModMessageSender",
            channelName = "TestChannel",
            message = "!overlay This is a message with the command prefix"
        )

        val lambda = mockk<(String) -> Unit>(relaxed = true)

        twitchChatParser.onIRCMessageEvent(event, lambda)

        verify(exactly = 1) { lambda.invoke("This is a message with the command prefix") }
    }

    @Test
    fun `message contains prefix and is from broadcaster`() {
        val event = generateIRCMessageEvent(
            subscriberMonths = 200,
            isBroadcaster = true,
            isModerator = false,
            isVip = false,
            color = "#FF4500",
            senderName = "TestChannel",
            channelName = "TestChannel",
            message = "!overlay This is a message with the command prefix"
        )

        val lambda = mockk<(String) -> Unit>(relaxed = true)

        twitchChatParser.onIRCMessageEvent(event, lambda)

        verify(exactly = 1) { lambda.invoke("This is a message with the command prefix") }
    }


}