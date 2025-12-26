package mod.lucky.java.loader

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.BaseDrop
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.processProps
import mod.lucky.common.drop.readDropStructure
import mod.lucky.common.drop.DropStructure
import mod.lucky.common.LOGGER
import mod.lucky.java.MinecraftNbtStructure
import mod.lucky.java.JAVA_GAME_API
import java.io.File
import java.util.zip.ZipFile

data class NbtStructureWithProps(
    val defaultProps: DictAttr,
    val structure: MinecraftNbtStructure,
)

fun readDropStructures(baseDir: File, structuresConfig: StructuresConfig): Map<String, DropStructure> {
    val dropStructures = getStructurePathsById(baseDir).mapNotNull { (k, path) ->
        try {
            when {
                k.endsWith(".luckystruct") -> {
                    val stream = getInputStream(baseDir, path)!!
                    val dropStructure = readDropStructure(readLines(stream))
                    k to dropStructure
                }
                k.endsWith(".schematic") -> {
                    val stream = getInputStream(baseDir, path)!!
                    val (props, drops) = readLegacySchematic(stream)
                    k to DropStructure(props, drops)
                }
                else -> null
            }
        } catch (e: Exception) {
            LOGGER.logError("Error reading structure '$k', path=${path}", e)
            null
        }
    }.toMap()

    return configureStructures(
        dropStructures.mapValues { (_, v) -> StructureWithProps(v.defaultProps, v) },
        structuresConfig,
    ).mapValues { (_, v) -> v.structure.copy(defaultProps = v.props) }
}

fun readNbtStructures(baseDir: File, structuresConfig: StructuresConfig): Map<String, NbtStructureWithProps> {
    val nbtStructures = getStructurePathsById(baseDir).mapNotNull { (k, path) ->
        try {
            if (k.endsWith(".nbt")) {
                val stream = getInputStream(baseDir, path)!!
                val (structure, size) = JAVA_GAME_API.readNbtStructure(stream)
                val props = dictAttrOf("size" to vec3AttrOf(AttrType.DOUBLE, size.toDouble()))
                k to NbtStructureWithProps(props, structure)
            } else null
        } catch (e: Exception) {
            LOGGER.logError("Error reading structure '$k', path=${path}", e)
            null
        }
    }.toMap()

    return configureStructures(
        nbtStructures.mapValues { (_, v) -> StructureWithProps(v.defaultProps, v) },
        structuresConfig,
    ).mapValues { (_, v) -> v.structure.copy(defaultProps = v.props) }
}
