package mod.lucky.common

interface Logger {
    fun logError(msg: String? = null, error: Exception? = null)
    fun logInfo(msg: String)
}

lateinit var LOGGER: Logger
