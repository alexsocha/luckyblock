package mod.lucky.java.loader

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.processProps
import mod.lucky.common.drop.readLuckyStructure
import mod.lucky.common.gameAPI
import mod.lucky.common.logger
import mod.lucky.java.NBTStructure
import mod.lucky.java.javaGameAPI
import java.io.File
import java.util.zip.ZipFile

interface StructureResource {
    val defaultProps: DictAttr
}

data class DropStructureResource(
    override val defaultProps: DictAttr,
    val drops: List<SingleDrop>,
) : StructureResource

data class NBTStructureResource(
    override val defaultProps: DictAttr,
    val structure: NBTStructure,
): StructureResource

fun readStructures(baseDir: File, configLines: List<String>): Map<String, StructureResource> {
    val structSpec = LuckyRegistry.dropSpecs["structure"]!!
    val defaultPropsSpec = structSpec.copy(children = structSpec.children.plus(
        "file" to ValueSpec(AttrType.STRING)
    ))

    val overridePropsList = splitLines(configLines).mapNotNull {
        try {
            val attr = parseEvalAttr(it,
                defaultPropsSpec,
                LuckyRegistry.parserContext,
                LuckyRegistry.simpleEvalContext
            ) as DictAttr
            SingleDrop.processProps("structure", attr)
        } catch (e: Exception) {
            logger.logError("Error reading structure settings: $it", e)
            null
        }
    }

    val filePaths: List<String> = try {
        if (baseDir.isDirectory) {
            baseDir.walk().toList().filter { !it.isDirectory }.map { it.relativeTo(baseDir).path }
        } else {
            ZipFile(baseDir).entries().toList().filter { !it.isDirectory }.map { it.name }
        }
    } catch (e: Exception) {
        logger.logError("Error searching for structures", e)
        emptyList()
    }
    val structPaths = filePaths
        .map { it.replace(File.separatorChar, '/') }
        .filter { it.startsWith("structures/") || it.contains("/structures/") }

    val structures = structPaths.mapNotNull { fullPath ->
        val path = fullPath.substring(fullPath.indexOf("structures/") + "structures/".length)

        try {
            val stream = getInputStream(baseDir, fullPath)!!
            when {
                path.endsWith(".luckystruct") -> {
                    val (props, drops) = readLuckyStructure(readLines(stream))
                    path to DropStructureResource(props, drops)
                }
                path.endsWith(".schematic") -> {
                    val (props, drops) = readLegacySchematic(stream)
                    path to DropStructureResource(props, drops)
                }
                path.endsWith(".nbt") -> {
                    val (structure, size) = javaGameAPI.readNBTStructure(stream)
                    val props = dictAttrOf("size" to vec3AttrOf(AttrType.DOUBLE, size.toDouble()))
                    path to NBTStructureResource(props, structure)
                }
                else -> null
            }
        } catch (e: Exception) {
            logger.logError("Error reading structure '$path'", e)
            null
        }
    }.toMap()

    //val overrideProps = overridePropsList.find { it.getValue<String>("file") == path } ?: DictAttr()
    val aliasedStructures = structures.mapNotNull { (_, structure) ->
        structure.defaultProps.getOptionalValue<String>("id")?.let { it to structure }
    }.toMap()

    val configuredStructures = overridePropsList.mapNotNull { props ->
        val path = props.getOptionalValue<String>("file")
        if (path == null || path !in structures) {
            logger.logError("Error in structures.txt: Structure with path '$path' doesn't exist")
            null
        } else props.getOptionalValue<String>("id")?.let {
            val newStruct = when (val struct = structures[path]!!) {
                is DropStructureResource -> struct.copy(defaultProps = props.withDefaults(struct.defaultProps.children))
                is NBTStructureResource -> struct.copy(defaultProps = props.withDefaults(struct.defaultProps.children))
                else -> struct
            }
            it to newStruct
        }
    }.toMap()

    return structures + aliasedStructures + configuredStructures
}