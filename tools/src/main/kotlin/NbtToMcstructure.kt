@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*

class NbtToMcstructure: Subcommand("nbt-to-mcstructure", "Convert .nbt structures (Java edition) to .mcstructure (Bedrock edition)") {
    val structurePath by argument(ArgType.String, description = "Path to .nbt file, or a folder containing .nbt structures")
    val outputStructuresFolder by option(ArgType.String, description = "Output folder for converted structures").default("structures")

    override fun execute() {
        println(structurePath)
    }
}
