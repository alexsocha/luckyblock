@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools.generateBedrockConfig

import kotlinx.cli.*
import mod.lucky.common.*
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.bedrock.common.registerBedrockTemplateVars
import mod.lucky.tools.*

import org.apache.commons.text.CaseUtils
import mod.lucky.java.loader.loadAddonResources
import java.io.File
import java.util.*

fun createUuid(blockId: String, suffix: String): String {
    val bedrockTemplateAddonUuid = UUID.nameUUIDFromBytes(
        blockId.toByteArray()
    ).toString()
    return UUID.nameUUIDFromBytes("${bedrockTemplateAddonUuid}-${suffix}".toByteArray()).toString()
}

fun replaceTemplateVariables(templateVariables: Map<String, String>, v: String): String {
    return templateVariables.entries.fold(v) { acc, (old, new) -> acc.replace("\${$old}", new) }
}

class GenerateBedrockConfig: Subcommand("generate-bedrock-addon", "Convert Lucky Block config files into a bedrock-compatible addon") {
    private val inputConfigFolder by option(ArgType.String, description = "Input config folder").default(".")
    private val inputAddonTemplateFolder by option(ArgType.String, description = "Input add-on template folder").default("../bedrock/template-addon")
    private val outputAddonFolder by option(ArgType.String, description = "Output Bedrock add-on folder").default("./addon")
    private val seed by option(ArgType.Int, description = "Random seed").default(0)

    fun prepare() {
        GAME_API = BedrockToolsGameAPI
        LOGGER = ToolsLogger
        registerCommonTemplateVars(GameType.BEDROCK)
        registerBedrockTemplateVars()
    }

    override fun execute() {
        this.prepare()

        val addonResources = loadAddonResources(File(inputConfigFolder))!!
        val blockIdWithNamespace = addonResources.addon.ids.block!!
        val blockIdWithoutNamespace = blockIdWithNamespace.split(':')[1]
        val blockDrops = addonResources.drops[blockIdWithNamespace]!!

        // generate .mcstructure files from block drops
        var (newBlockDrops, mcStructureDropsAcc) = generateDrops(blockDrops, blockIdWithoutNamespace, seed)

        // generate .mcstructure files from drop structures (.luckystruct files)
        val newDropStructures = addonResources.dropStructures.mapValues { (_, v) ->
            val (newDrops, mcStructureDrops) = generateDrops(
                v.drops.map { WeightedDrop(it, "") },
                blockIdWithoutNamespace,
                seed,
                mcStructureDropsAcc
            )
            mcStructureDropsAcc = mcStructureDrops
            DropStructure(v.defaultProps, newDrops)
        }

        // format drops and drop structures to use in serverScript.js
        val indent = "    "
        val jsDrops = "`\n" +
            newBlockDrops.joinToString("\n") { dropToString(it).replace("`", "\\`") } +
            "$indent\n`"

        val jsDropStructures = "{\n" +
            newDropStructures.map { (k, v) -> "$indent$indent\"$k\": `\n" +
                dropStructureToString(v).joinToString("\n") {
                    it.replace("`", "\\`")
                } + "\n`,"
            }.joinToString("\n") +
            "\n$indent}"

        val templateVariables = mapOf(
            "blockId" to blockIdWithoutNamespace,
            "addonId" to CaseUtils.toCamelCase(blockIdWithoutNamespace, true, '_'),
            "blockName" to blockIdWithoutNamespace.split("_").joinToString(" ") { it.capitalize() },
            "behaviorPackUuid" to createUuid(blockIdWithNamespace, "behavior-pack"),
            "behaviorPackModuleUuid" to createUuid(blockIdWithNamespace, "behavior-pack-module"),
            "resourcePackUuid" to createUuid(blockIdWithNamespace, "resource-pack"),
            "resourcePackModuleUuid" to createUuid(blockIdWithNamespace, "resource-pack-module"),
            "drops" to jsDrops,
            "dropStructures" to jsDropStructures,
        )

        // copy addon resources
        val behaviorPackFolder = File(inputAddonTemplateFolder).resolve("./behavior_pack")
        val resourcePackFolder = File(inputAddonTemplateFolder).resolve("./resource_pack")
        for (inputFile in behaviorPackFolder.walk() + resourcePackFolder.walk()) {
            if (inputFile.isFile) {
                val relativeInputFilePath = inputFile.relativeTo(File(inputAddonTemplateFolder)).path
                if (inputFile.extension in ("json, lang")) {
                    val inputText = inputFile.readText()
                    val outputText = replaceTemplateVariables(templateVariables, inputText)

                    val outputFile = File(outputAddonFolder).resolve(
                        replaceTemplateVariables(templateVariables, relativeInputFilePath)
                    )
                    outputFile.parentFile.mkdirs()
                    outputFile.writeText(outputText)
                } else {
                    val outputFile = File(outputAddonFolder).resolve(relativeInputFilePath)
                    inputFile.copyTo(outputFile)
                }
            }
        }

        // write all pre-generated .mcstructure drops
        mcStructureDropsAcc.structures.forEach { (k, v) ->
            val outputFile = File(outputAddonFolder).resolve("behavior_pack/structures/lucky/${k}.mcstructure")
            outputFile.parentFile.mkdirs()
            writeNbtFile(outputFile, v, compressed=false, isLittleEndian=true)
        }
    }
}
