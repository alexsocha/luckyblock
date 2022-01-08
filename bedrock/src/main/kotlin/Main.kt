package mod.lucky.bedrock

external val server: MCServer
external class ServerScriptJs {
    val serverSystem: MCServerSystem
}

val serverScript: ServerScriptJs = js("require(\"../../../../../bedrock/build/lucky-config/behavior_pack/scripts/serverScript.js\")")

fun main() {
    initServer(server, serverScript.serverSystem)
}
