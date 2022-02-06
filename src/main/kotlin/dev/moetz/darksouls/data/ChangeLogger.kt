package dev.moetz.darksouls.data

import java.io.File
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

}