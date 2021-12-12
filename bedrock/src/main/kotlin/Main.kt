package mod.lucky.bedrock

external val server: MCServer
external class GeneratedConfigJS {
    val serverSystem: MCServerSystem
}

val generatedConfig: GeneratedConfigJS = js("require(\"../../../../../bedrock/build/processedResources/generated-config.js\")")

fun main() {
    initServer(server, generatedConfig.serverSystem)
}
