package dev.moetz.darksouls.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.IRCMessageEvent
import kotlin.concurrent.thread

class TwitchChatParser(
    private val twitchClient: TwitchClient,
    private val messagePrefix: String,
    private val channelName: String
) {

    fun startChatObservingThread(commandTriggeredCallback: (commandPayload: String) -> Unit) {
        thread(start = true) {
            twitchClient.chat.joinChannel(channelName)

            twitchClient.eventManager.onEvent(IRCMessageEvent::class.java) { event ->
                if (event.isMessage() && (event.isModerator() || event.isBroadcaster()) && event.isCommand(messagePrefix)) {
                    commandTriggeredCallback.invoke(event.message.orElse("").substring(messagePrefix.length + 1))
                }
            }
        }
    }

    private fun IRCMessageEvent.isMessage(): Boolean = this.commandType == "PRIVMSG"

    private fun IRCMessageEvent.isModerator(): Boolean =
        (this.getTagValue("user-type").orElse("") == "mod" || this.badges["moderator"] == "1")

    private fun IRCMessageEvent.isBroadcaster(): Boolean = this.badges["broadcaster"] == "1"

    private fun IRCMessageEvent.isCommand(command: String): Boolean =
        this.message.orElse("").startsWith(command, ignoreCase = true) &&
                this.message.orElse("").length > (command.length + 1)   //command length + 1 space
}