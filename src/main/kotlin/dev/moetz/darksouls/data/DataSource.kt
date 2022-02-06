package dev.moetz.darksouls.data

import java.io.File

class DataSource(
    private val contentFile: File,
    private val colorFile: File
) {

    init {
        if (contentFile.exists().not()) {
            contentFile.createNewFile()
        }
        if (colorFile.exists().not()) {
            colorFile.createNewFile()
        }
    }

    fun getContent(): String {
        return try {
            contentFile.readText()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    fun getColor(): String? {
        return try {
            colorFile.readText().takeIf { it.isNotBlank() }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    fun setContent(content: String) {
        contentFile.writeText(content)
    }

    fun setColor(color: String) {
        colorFile.writeText(color)
    }


}