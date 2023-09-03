@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("luckytools", strictSubcommandOptionsOrder = true)
    parser.subcommands(GenerateBedrockDrops(), NbtToMcstructure(), DownloadBlockIds())
    parser.parse(args)
}
