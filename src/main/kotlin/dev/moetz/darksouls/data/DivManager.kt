package dev.moetz.darksouls.data

import io.ktor.util.*

class DivManager(
    private val fontPath: String
) {

    fun dataUpdateToHtmlDiv(dataUpdate: DataManager.DataUpdate): String {
        return "<style>@font-face{font-family: darksoulsfont;src: url($fontPath);}</style>" +
                "<div style=\"color:#${dataUpdate.color ?: "FFFFFF"};font-family: darksoulsfont;\">" + dataUpdate.content.escapeHTML().replace("\\n", "<br />") + "</div>"
    }

}