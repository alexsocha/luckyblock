@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import mod.lucky.tools.uploadToCurseForge.UploadToCurseForge
import mod.lucky.tools.generateBedrockAddon.GenerateBedrockAddon
import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("luckytools", strictSubcommandOptionsOrder = true)

    parser.subcommands(GenerateBedrockAddon(), NbtToMcstructure(), DownloadBlockIds(), UploadToCurseForge())
    parser.parse(args)
}
