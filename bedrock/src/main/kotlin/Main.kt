package mod.lucky.bedrock

external val server: MCServer
external class ServerScriptJs {
    val serverSystem: MCServerSystem
}

val serverScript: ServerScriptJs = js("require(\"../../../../../bedrock/build/processedResources/serverScript.js\")")

fun main() {
    initServer(server, serverScript.serverSystem)
}
