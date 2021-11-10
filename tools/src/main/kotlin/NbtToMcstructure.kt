@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*
import mod.lucky.common.attribute.*
import java.nio.ByteOrder
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

fun nbtToMcstructure(nbtStructureAttr: Attr): Attr {
    println(attrToString(nbtStructureAttr))
    return DictAttr()
}

class NbtToMcstructure: Subcommand("nbt-to-mcstructure", "Convert .nbt structures (Java edition) to .mcstructure (Bedrock edition)") {
    val structurePath by argument(ArgType.String, description = "Path to .nbt file, or a folder containing .nbt structures")
    val outputStructuresFolder by option(ArgType.String, description = "Output folder for converted structures").default("structures")

    override fun execute() {
        val structureFiles = if (!File(structurePath).isDirectory()) listOf(File(structurePath)) else {
            File(structurePath).walkTopDown().filter { it.extension == "nbt" }.toList()
        }

        val mcStructureAttrs = listOf(structureFiles[0]).map {
            println(it)
            val nbtStructureAttr = readCompressedNbt(GZIPInputStream(FileInputStream(it)), ByteOrder.BIG_ENDIAN)
            val stream = GZIPInputStream(FileInputStream(it))
            nbtToMcstructure(nbtStructureAttr)
        }

        structureFiles.zip(mcStructureAttrs).forEach { (structureFile, mcStructureAttr) ->
            val nbtBuffer = attrToNbt(mcStructureAttr, ByteOrder.LITTLE_ENDIAN)

            val relativePath = structureFile.relativeTo(File(structurePath))
            val outputFile = File(outputStructuresFolder)
                .resolve(relativePath.parentFile?.name ?: "")
                .resolve("${relativePath.nameWithoutExtension}.mcstructure")

            writeBufferToFile(nbtBuffer, outputFile)
        }
    }
}
