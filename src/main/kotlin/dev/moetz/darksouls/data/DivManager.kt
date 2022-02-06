package dev.moetz.darksouls.data

import io.ktor.util.*

class DivManager {

    fun dataUpdateToHtmlDiv(dataUpdate: DataManager.DataUpdate): String {
        val innerHtml = dataUpdate.content.escapeHTML().newLinesAsBreak()
        return "<div style=\"color:#${dataUpdate.color};font-family: darksoulsfont;\">$innerHtml</div>"
    }

    private fun String.newLinesAsBreak(): String = this.replace("\\n", "<br />")

}