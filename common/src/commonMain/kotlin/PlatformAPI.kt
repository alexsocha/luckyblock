package mod.lucky.common

interface PlatformAPI {
    fun evalJS(script: String): Any
}

lateinit var PLATFORM_API: PlatformAPI
