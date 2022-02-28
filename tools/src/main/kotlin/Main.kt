@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import mod.lucky.tools.uploadToCurseForge.UploadToCurseForge
import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("luckytools", strictSubcommandOptionsOrder = true)

    parser.subcommands(GenerateBedrockDrops(), NbtToMcstructure(), DownloadBlockIds(), UploadToCurseForge())

    parser.parse(args)
}
