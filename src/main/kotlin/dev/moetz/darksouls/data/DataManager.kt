package dev.moetz.darksouls.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DataManager(
    private val dataSource: DataSource,
    private val changeLogger: ChangeLogger
) {

    data class DataUpdate(
        val content: String,
        val color: String
    )

    private val changeMutex = Mutex()

    private val mutableStateFlow: MutableStateFlow<DataUpdate> by lazy {
        MutableStateFlow(
            DataUpdate(
                content = dataSource.getContent(),
                color = dataSource.getColor() ?: "FFFFFF"
            )
        )
    }

    val dataUpdateFlow: StateFlow<DataUpdate> by lazy { mutableStateFlow }

    fun getData(): DataUpdate {
        return dataUpdateFlow.value
    }

    suspend fun update(content: String, color: String?) {
        val whiteListedColor = (color ?: getData().color)
            ?.takeIf { colorCandidate -> colorCandidate.all { it in 'A'..'F' || it in 'a'..'f' || it in '0'..'9' } }
            ?.takeIf { it.length == 6 }
            ?: "FFFFFF"

        changeMutex.withLock {
            changeLogger.log(content, whiteListedColor)
            dataSource.setContent(content)
            dataSource.setColor(whiteListedColor)
            mutableStateFlow.value = DataUpdate(
                content = content,
                color = (color ?: dataSource.getColor()) ?: "FFFFFF"
            )
        }
    }

}