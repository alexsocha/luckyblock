package mod.lucky.java

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

import mod.lucky.common.gameAPI
import mod.lucky.java.loader.getConfigDir
import mod.lucky.java.loader.getInputStream
import mod.lucky.java.loader.readLines
import java.io.BufferedWriter
import java.io.FileWriter

data class ModNotificationState(
    val didShowUpdate: Map<String, Boolean?> = HashMap(),
    val didCheckForUpdates: Boolean = false,
) { companion object }

private fun getModVersionInt(modVersionStr: String): Int {
    val splitVersion = modVersionStr.split('-')
    val modVersion = splitVersion[1].toInt()
    val mcVersion = splitVersion[0].split('.')
    return mcVersion[0].toInt() * 1000000 + mcVersion[1].toInt() * 10000 +
        (mcVersion.getOrNull(2)?.toInt() ?: 0) * 100 +
        modVersion
}

fun ModNotificationState.Companion.fromCache(): ModNotificationState {
    try {
        val stream = getInputStream(getConfigDir(JAVA_GAME_API.getGameDir()), ".showupdate.cache") ?: return ModNotificationState()
        val lines = readLines(stream)
        return ModNotificationState(
            didShowUpdate = lines.map { it to true }.toMap()
        )
    } catch (e: Exception) {
        gameAPI.logError("Error reading .showupdate.cache", e)
        return ModNotificationState()
    }
}

fun appendNotificationCache(modVersion: Int) {
   try {
       val file = getConfigDir(JAVA_GAME_API.getGameDir()).resolve(".showupdate.cache")
       val bw = BufferedWriter(FileWriter(file))
       bw.appendLine(modVersion.toString())
       bw.close()
   } catch (e: Exception) {
       gameAPI.logError("Error writing .showupdate.cache", e)
   }
}


fun checkForUpdates(state: ModNotificationState): ModNotificationState {
    // modVersionStr format: <MC Version>-<Lucky Version>-<forge/fabric>
    try {
        val settings = JavaLuckyRegistry.globalSettings
        if (settings.checkForUpdates && !state.didCheckForUpdates) {
            val curModVersionInt = getModVersionInt(JAVA_GAME_API.getModVersion())
            val url = URL("https://www.luckyblockmod.com/version-log-${JAVA_GAME_API.getLoaderName()}")
            val reader = BufferedReader(InputStreamReader(url.openStream()))
            for (line in generateSequence { reader.readLine() }) {
                val splitLine = line.split("|").toTypedArray()
                if (splitLine.size < 3) continue
                val logVersionNumber = splitLine[0].toInt()

                // log version numbers <= 0 are reserved for special announcements
                if (logVersionNumber.toString() !in state.didShowUpdate && (logVersionNumber <= 0 || logVersionNumber > curModVersionInt)) {
                    JAVA_GAME_API.showClientMessage(splitLine[2])
                    appendNotificationCache(logVersionNumber)
                    return state.copy(
                        didCheckForUpdates = true,
                        didShowUpdate = state.didShowUpdate.plus(logVersionNumber.toString() to true),
                    )
                } else {
                    break
                }
            }
        }
    } catch (e: Exception) {
        gameAPI.logError("Error checking for updates", e)
    }
    return state.copy(didCheckForUpdates = true)
}
