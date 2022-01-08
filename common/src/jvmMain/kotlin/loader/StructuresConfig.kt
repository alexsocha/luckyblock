package mod.lucky.java.loader

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.BaseDrop
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.processProps
import mod.lucky.common.drop.readDropStructure
import mod.lucky.common.drop.DropStructure
import mod.lucky.common.LOGGER
import mod.lucky.java.JAVA_GAME_API
import java.io.File
import java.util.zip.ZipFile

fun getStructureFilesById(baseDir: File): Map<String, File> {
    val filePaths: List<String> = try {
        if (baseDir.isDirectory) {
            baseDir.walk().toList().filter { !it.isDirectory }.map { it.relativeTo(baseDir).path }
        } else {
            ZipFile(baseDir).entries().toList().filter { !it.isDirectory }.map { it.name }
        }
    } catch (e: Exception) {
        LOGGER.logError("Error searching for structures", e)
        emptyList()
    }
    return filePaths
        .map { it.replace(File.separatorChar, '/') }
        .filter { it.startsWith("structures/") || it.contains("/structures/") }
        .map {
            val relativePath = it.substring(it.indexOf("structures/") + "structures/".length)
            relativePath to File(it)
        }.toMap()
}

data class StructuresConfig(
    val structurePropsById: Map<String, DictAttr>
) {
    companion object {
        fun read(baseDir: File, configLines: List<String>): StructuresConfig {
            val structSpec = LuckyRegistry.dropSpecs["structure"]!!
            val defaultPropsSpec = structSpec.copy(children = structSpec.children.plus(
                "file" to ValueSpec(AttrType.STRING)
            ))

            val structureFilesById = getStructureFilesById(baseDir)
            val structurePropsById = splitLines(configLines).mapNotNull {
                val props = try {
                    val rawProps = parseEvalAttr(it,
                        defaultPropsSpec,
                        LuckyRegistry.parserContext,
                        LuckyRegistry.simpleEvalContext
                    ) as DictAttr
                    SingleDrop.processProps("structure", rawProps)
                } catch (e: Exception) {
                    LOGGER.logError("Error in structures.txt: Error parsing '$it'", e)
                    return@mapNotNull null
                }

                val path = props.getOptionalValue<String>("file")
                if (path == null) {
                    LOGGER.logError("Error in structures.txt: '$it' is missing a 'file' attribute")
                    return@mapNotNull null
                }
                if (path !in structureFilesById) {
                    LOGGER.logError("Error in structures.txt: Structure with path '$path' not found")
                    return@mapNotNull null
                }

                props.getWithDefault("id", path) to props
            }.toMap()

            return StructuresConfig(structurePropsById)
        }
    }
}

data class StructureWithProps<T>(
    val props: DictAttr,
    val structure: T,
)
/*
 * Merges the properties in StructuresConfig with the individual properties of each structure, and
 * creates ID aliases.
 */
fun <T>configureStructures(
    structures: Map<String, StructureWithProps<T>>,
    structuresConfig: StructuresConfig,
): Map<String, StructureWithProps<T>> {
    val aliasedStructures = structures.mapNotNull { (_, structure) ->
        structure.props.getOptionalValue<String>("id")?.let { it to structure }
    }.toMap()

    val configuredStructures = structuresConfig.structurePropsById.mapNotNull { (k, overrideStructureProps) ->
        val path = overrideStructureProps.getValue<String>("file")
        val structure = structures[path] ?: return@mapNotNull null
        val newProps = overrideStructureProps.withDefaults(structure.props.children)
        k to structure.copy(props=newProps)
    }.toMap()

    return structures + aliasedStructures + configuredStructures
}

