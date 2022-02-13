package dev.moetz.darksouls.data

import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListener
import java.io.File
import java.io.FileNotFoundException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ChangeLogger(
    private val logFile: File
) {

    init {
        if (logFile.exists().not()) {
            logFile.createNewFile()
        }
    }

    fun log(newContent: String, newColor: String) {
        try {
            logFile.appendText(
                "${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} $newColor '$newContent'\n"
            )
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    fun getLogHtmlWebsocketFlow(): Flow<String> {
        return getLastXLogEntriesFlow(10)
            .map { list ->
                buildString {
                    list
                        .forEach { entry ->
                            append(entry.dateTime.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm:ss")))
                            append(": ")
                            appendLine(entry.content.escapeHTML())
                        }
                }
            }
    }

    private fun getLastXLogEntriesFlow(numberOfLogLines: Int): Flow<List<LogEntry>> {
        return getLogEntriesFlow()
            .runningFold(emptyList<LogEntry>()) { acc, value ->
                val newList = (acc + value).sortedByDescending { it.dateTime }
                if (newList.size > numberOfLogLines) {
                    newList.dropLast(newList.size - numberOfLogLines)
                } else {
                    newList
                }
            }
    }

    private data class LogEntry(
        val dateTime: ZonedDateTime,
        val color: String,
        val content: String
    )


    private fun getLogEntriesFlow(): Flow<LogEntry> {
        return callbackFlow<String> {
            val listener = object : TailerListener {
                override fun init(tailer: Tailer?) {
//                    println("tailer init: ${file.absolutePath}")
                }

                override fun fileNotFound() {
                    close(FileNotFoundException("File ${logFile.absolutePath} not found."))
                }

                override fun fileRotated() {
                    println("fileRotated: ${logFile.absolutePath}")
                }

                override fun handle(line: String?) {
//                    println("handle(${file.absolutePath}): $line")
                    line?.let { trySend(it) }
                }

                override fun handle(ex: Exception?) {
                    close(ex ?: RuntimeException("handle exception, but exception was null"))
                }
            }
            val tailer = Tailer.create(logFile, listener, 500, false)
            this.awaitClose { tailer?.stop() }
        }
            .mapNotNull { line ->
                val split = line.split(" ", limit = 3)
                if (split.size == 3) {
                    LogEntry(
                        dateTime = ZonedDateTime.parse(split[0], DateTimeFormatter.ISO_ZONED_DATE_TIME),
                        color = split[1],
                        content = split[2].drop(1).dropLast(1)
                    )
                } else {
                    null
                }
            }
            .flowOn(Dispatchers.IO)
    }

}