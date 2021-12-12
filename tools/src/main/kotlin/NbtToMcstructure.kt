@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*
import mod.lucky.common.attribute.*
import mod.lucky.common.Vec3i
import mod.lucky.common.GameType
import java.nio.ByteOrder
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

fun nbtToMcstructure(blockConversions: BlockConversions, nbtStructure: DictAttr): Attr {
    val size = nbtStructure.getList("size").toVec3<Int>()

    val blockGridPositions = (0 until size.x).flatMap { x ->
        (0 until size.y).flatMap { y ->
            (0 until size.z).map { z ->
                Vec3i(x, y, z)
            }
        }
    }

    val nbtBlocks = nbtStructure.getList("blocks").toList().map { it -> it as DictAttr }

    val structureWorldOrigin = Vec3i(
        nbtBlocks.minOf { it.getVec3<Int>("pos").x },
        nbtBlocks.minOf { it.getVec3<Int>("pos").y },
        nbtBlocks.minOf { it.getVec3<Int>("pos").z },
    )

    val blockIndices = blockGridPositions.map { gridPos ->
        val nbtBlock = nbtBlocks.find {
            val absolutePos = it.getVec3<Int>("pos")
            val relativePos = absolutePos - structureWorldOrigin
            relativePos == gridPos
        }
        // -1 represents a 'void' block, which will not replace any blocks in the world when the
        // structure is placed
        nbtBlock?.getValue<Int>("state") ?: -1
    }

    val palette = nbtStructure.getList("palette").toList().map {
        val nbtBlock = it as DictAttr
        println(nbtBlock)

        val javaBlockId = nbtBlock.getValue<String>("Name")
        val javaBlockStates = nbtBlock.getWithDefault("Properties", DictAttr()).children.mapValues { (_, v) -> BlockState.fromAttr(v) }

        val (bedrockBlockId, bedrockBlockStates) = try {
            blockConversions.convert(GameType.JAVA, GameType.BEDROCK, javaBlockId, javaBlockStates)
        } catch (e: BlockConversionException) {
            println("Using 'minecraft:air' instead of '${javaBlockId}': ${e.message}")
            Pair("minecraft:air", emptyMap())
        }

        dictAttrOf(
            "name" to stringAttrOf(bedrockBlockId),
            "states" to DictAttr(bedrockBlockStates.mapValues { (_, v) -> v.toAttr() }),
        )
    }
    /*
    val sortedNbtBlocks = nbtBlocks.sortedWith(tripleComparator {
        val pos = it.getVec3<Int>("pos"
        Triple(pos.x, pos.y, pos.z)
    })
    */

    val result = dictAttrOf(
        "format_version" to intAttrOf(1),
        "size" to vec3AttrOf(AttrType.INT, size),
        "structure" to dictAttrOf(
            "block_indices" to ListAttr(blockIndices.map { intAttrOf(it) }),
            "palette" to dictAttrOf(
                "default" to dictAttrOf(
                    "block_palette" to listAttrOf(
                        dictAttrOf(
                            "name" to stringAttrOf(""),
                            "states" to DictAttr(),
                            "version" to intAttrOf(17825806),
                        ),
                    ),
                    "block_position_data" to DictAttr(),
                ),
            ),
            "entities" to listAttrOf(),
        ),
        "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0)),
    )
    println(result)
    return result
}

class NbtToMcstructure: Subcommand("nbt-to-mcstructure", "Convert .nbt structures (Java edition) to .mcstructure (Bedrock edition)") {
    val structurePath by argument(ArgType.String, description = "Path to .nbt file, or a folder containing .nbt structures")
    val outputStructuresFolder by option(ArgType.String, description = "Output folder for converted structures").default("structures")
    val blockIdsFile by option(ArgType.String, description = "Path to .yaml file with block ID conversions").default("block_ids.yaml")
    val blockConversionFile by option(ArgType.String, description = "Path to .yaml file with block conversions").default("block_conversion.yaml")
    val outputGeneratedBlockConversionFile by option(ArgType.String, description = "Path to .yaml file with block conversions after parsing regex").default(".debug/block_conversion.generated.yaml")

    override fun execute() {
        val blockConversions = BlockConversions.readAndParseRegexFromYamlFile(File(blockConversionFile))

        File(outputGeneratedBlockConversionFile).parentFile.mkdirs()
        File(outputGeneratedBlockConversionFile).writeText(blockConversions.toYaml())

        val structureFiles = if (!File(structurePath).isDirectory()) listOf(File(structurePath)) else {
            File(structurePath).walkTopDown().filter { it.extension == "nbt" }.toList()
        }

        val mcStructureAttrs = listOf(structureFiles[6]).map {
            println(it.name)
            val nbtStructureAttr = readNbtFile(it, compressed=true, isLittleEndian=false)
            nbtToMcstructure(blockConversions, nbtStructureAttr as DictAttr)
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
