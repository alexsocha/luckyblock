package mod.lucky.common

interface PlatformAPI {
    fun evalJS(script: String): Any
}

lateinit var platformAPI: PlatformAPI
