@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*
import mod.lucky.common.*
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.action.calculatePos
import mod.lucky.bedrock.common.registerBedrockTemplateVars

import mod.lucky.java.loader.DropStructureResource
import mod.lucky.java.loader.loadAddonResources
import mod.lucky.java.loader.loadMainResources
import java.nio.ByteOrder
import java.io.File

data class GeneratedDrops(
    val dropStructures: HashMap<String, DictAttr>,
    // drop string -> List<structreId>
    val dropStructureCache: HashMap<String, List<String>>
)

class SeededRandom(
    seed: Int,
    private val random: kotlin.random.Random = kotlin.random.Random(seed),
) : Random {
    override fun randInt(range: IntRange): Int = range.random(random)
    override fun nextDouble(): Double = random.nextDouble()
}

class SpyRandom(
    private val random: Random,
    private var wasUsed: Boolean = false,
) : Random {
    override fun randInt(range: IntRange): Int {
        wasUsed = true
        return random.randInt(range)
    }
    override fun nextDouble(): Double {
        wasUsed = true
        return random.nextDouble()
    }
    fun wasUsed(): Boolean = wasUsed
}

fun getDropAttrOrDefault(drop: SingleDrop, k: String): Attr {
    if (k in drop.props) return drop.props[k]!!
    val defaultValue = LuckyRegistry.dropDefaults[drop.type]!![k]!!
    val spec = LuckyRegistry.dropSpecs[drop.type]!!.children[k]!!
    return if (defaultValue is Attr) defaultValue else ValueAttr((spec as ValueSpec).type!!, defaultValue)
}

fun createDropStructure(dropType: String, drops: List<SingleDrop>, random: Random): DictAttr {
    return when(dropType) {
        "item" -> dictAttrOf(
            "format_version" to intAttrOf(1),
            "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
            "structure" to dictAttrOf(
                "block_indices" to listAttrOf(ListAttr(), ListAttr()),
                "palette" to DictAttr(),
                "entities" to ListAttr(drops.map {
                    val dropProps = it.props
                    val pos = calculatePos(it, Vec3d(0.0, 0.0, 0.0))
                    dictAttrOf(
                        "Pos" to listAttrOf(
                            floatAttrOf((pos.x + 0.5 + (random.randDouble(0.0, 1.0) - 0.5)).toFloat()),
                            floatAttrOf((pos.y + 0.5).toFloat()),
                            floatAttrOf((pos.z + 0.5 + (random.randDouble(0.0, 1.0) - 0.5)).toFloat()),
                        ),
                        "identifier" to stringAttrOf("minecraft:item"),
                        "Item" to dictAttrOf(
                            "Name" to stringAttrOf(getIdWithNamespace(dropProps.getValue("id"))),
                            "Count" to ValueAttr(AttrType.BYTE, 1.toByte()),
                            "Damage" to ValueAttr(AttrType.SHORT, dropProps.getWithDefault("data", 0).toShort()),
                            *(if ("nbttag" in dropProps) arrayOf("tag" to dropProps.getDict("nbttag")) else emptyArray()),
                        ),
                    )
                }),
            ),
            "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0)),
        )
        "entity" -> dictAttrOf(
            "format_version" to intAttrOf(1),
            "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
            "structure" to dictAttrOf(
                "block_indices" to listAttrOf(ListAttr(), ListAttr()),
                "palette" to DictAttr(),
                "entities" to ListAttr(drops.map {
                    val dropProps = it.props
                    val pos = calculatePos(it, Vec3d(0.0, 0.0, 0.0))
                    dictAttrOf(
                        "Pos" to listAttrOf(
                            floatAttrOf((pos.x + 0.5).toFloat()),
                            floatAttrOf(pos.y.toFloat()),
                            floatAttrOf((pos.z + 0.5).toFloat()),
                        ),
                        "identifier" to stringAttrOf(getIdWithNamespace(dropProps.getValue("id"))),
                    ).with(dropProps.getDict("nbttag").children)
                }),
            ),
            "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0)),
        )
        "block" -> {
            val dropProps = drops.first().props
            val pos = calculatePos(drops.first(), Vec3d(0.0, 0.0, 0.0)).floor()
            dictAttrOf(
                "format_version" to intAttrOf(1),
                "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
                "structure" to dictAttrOf(
                    "block_indices" to listAttrOf(listAttrOf(intAttrOf(0)), listAttrOf(intAttrOf(-1))),
                    "palette" to dictAttrOf(
                        "default" to dictAttrOf(
                            "block_palette" to listAttrOf(
                                dictAttrOf(
                                    "name" to stringAttrOf(getIdWithNamespace(dropProps.getValue("id"))),
                                    "states" to dropProps.getWithDefault("state",  DictAttr()),
                                    "version" to intAttrOf(17825806),
                                ),
                            ),
                            "block_position_data" to dictAttrOf(
                                "0" to dictAttrOf(
                                    "block_entity_data" to dropProps.getDict("nbttag").withDefaults(mapOf(
                                        "x" to intAttrOf(pos.x),
                                        "y" to intAttrOf(pos.y),
                                        "z" to intAttrOf(pos.z),
                                    )),
                                ),
                            ),
                        ),
                    ),
                    "entities" to listAttrOf(),
                ),
                "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0)),
            )
        }
        else -> dictAttrOf("x" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)))
    }
}

fun generateSingleDrop(drop: SingleDrop, seed: Int, blockId: String, generatedDrops: GeneratedDrops): Pair<SingleDrop, GeneratedDrops> {
    val shouldGenerate = when(drop.type) {
        "item" -> "nbttag" in drop.props || "data" in drop.props
        "block" -> "nbttag" in drop.props
        "entity" -> "nbttag" in drop.props
        else -> false
    }
    if (!shouldGenerate) return Pair(drop, generatedDrops)

    val dropSamples = drop.props.getWithDefault("samples", 2)
    val dropSeed = drop.props.getWithDefault("seed", seed)
    val onePerSample = drop.props.getWithDefault("onePerSample", false)

    val propKeysForCombinedSamples = listOf("amount", "reinitialize", "posOffset", "centerOffset", "rotation")

    val dropProps = dictAttrOf(
        "type" to stringAttrOf(drop.type),
        "id" to drop.props["id"],
        "samples" to intAttrOf(dropSamples),
        "seed" to intAttrOf(dropSeed),
        "onePerSample" to booleanAttrOf(onePerSample),
        "nbttag" to drop.props["nbttag"],
        *(if (!onePerSample) propKeysForCombinedSamples.map { it to drop.props[it] }.toTypedArray() else emptyArray()),
        "amount" to if (onePerSample) intAttrOf(1) else drop.props["amount"],
        *(if (drop.type == "item" && "data" in drop.props) arrayOf("data" to drop.props["data"]) else emptyArray()),
        *(if (drop.type == "block") arrayOf("state" to drop.props["state"]) else emptyArray()),
    )
    val cacheKey = attrToSerializedString(dropProps)

    val seededRandom = SeededRandom(dropSeed)
    val spyRandom = SpyRandom(seededRandom)
    val evalContext = EvalContext(
        templateVarFns = LuckyRegistry.templateVarFns,
        templateContext = DropTemplateContext(drop = null, dropContext = null, random = spyRandom),
    )

    fun generate(): DictAttr {
        val evaluatedDrops = evalDrop(SingleDrop(drop.type, dropProps), evalContext)
        return createDropStructure(drop.type, evaluatedDrops, seededRandom)
    }

    val hasCached = cacheKey in generatedDrops.dropStructureCache
    val (structureIds, newGeneratedDrops) =
        if (hasCached) Pair(generatedDrops.dropStructureCache[cacheKey]!!, generatedDrops)
        else {
            val firstStructure = try {
                generate()
            } catch (e: MissingDropContextException) {
                ToolsLogger.logError("Can't generate drop which relies on in-game context: ${dropToString(drop)}")
                throw e
            }

            val dropStructurePrefix = "${blockId}_drop_"
            val newStructures: List<Pair<String, DictAttr>> = if (spyRandom.wasUsed()) {
                (0 until dropSamples).mapIndexed { i, it ->
                    val k = dropStructurePrefix +
                        "${generatedDrops.dropStructureCache.size + 1}" +
                        if (dropSamples > 1) ".${it + 1}" else ""

                    k to if (i == 0) firstStructure else generate()
                }
            } else {
                listOf("${dropStructurePrefix}${generatedDrops.dropStructureCache.size + 1}" to firstStructure)
            }

            val newStructureIds = newStructures.map { it.first }
            generatedDrops.dropStructureCache[cacheKey] = newStructureIds
            generatedDrops.dropStructures.putAll(newStructures)
            Pair(newStructureIds, generatedDrops)
        }

    val structureIdAttr = if (structureIds.size == 1) {
        stringAttrOf("lucky:${structureIds.first()}")
    } else {
        val templateVar = TemplateVar("randList", ListAttr(structureIds.map { stringAttrOf("lucky:${it}") }))
        TemplateAttr(
            spec = ValueSpec(AttrType.STRING),
            templateVars = listOf(Pair(null, templateVar))
        )
    }

    // ignore props which have no real-time effect, or which have already been accounted for
    val ignoredProps = listOf("samples", "seed", "onePerSample", "nbttag") +
        if (!onePerSample) propKeysForCombinedSamples else emptyList()

    // force-include props which have different defaults for structures
    val includedProps = if (drop.type == "entity") listOf("adjustY") else emptyList()

    val newDrop = SingleDrop(
        type = "structure",
        props = DictAttr(
            drop.props.children.plus(
                mapOf(
                    "type" to stringAttrOf("structure"),
                    "id" to structureIdAttr,
                ) + includedProps.associateWith { getDropAttrOrDefault(drop, it) }
            ).minus(ignoredProps)
        ),
    )
    return Pair(newDrop, newGeneratedDrops)
}

fun <T : BaseDrop> replaceNbtWithGeneratedDrops(drop: T, seed: Int, blockId: String, generatedDrops: GeneratedDrops): Pair<T, GeneratedDrops> {
    @Suppress("UNCHECKED_CAST")
    return when (drop) {
        is WeightedDrop -> {
            val (newDrop, newGeneratedDrops) = replaceNbtWithGeneratedDrops(drop.drop, seed, blockId, generatedDrops)
            Pair(drop.copy(drop = newDrop) as T, newGeneratedDrops)
        }
        is GroupDrop -> {
            var allGeneratedDrops = generatedDrops
            val newDrops = drop.drops.map {
                val (newDrop, newGeneratedDrops) = replaceNbtWithGeneratedDrops(it, seed, blockId, generatedDrops)
                allGeneratedDrops = newGeneratedDrops
                newDrop
            }
            Pair(drop.copy(drops = newDrops) as T, allGeneratedDrops)
        }
        is SingleDrop -> generateSingleDrop(drop, seed, blockId, generatedDrops) as Pair<T, GeneratedDrops>
        else -> throw Exception()
    }
}

fun createEmptyGeneratedDrops(): GeneratedDrops {
    return GeneratedDrops(
        dropStructures = HashMap(),
        dropStructureCache = HashMap(),
    )
}

fun prepareToGenerateDrops() {
    GAME_API = BedrockToolsGameAPI
    LOGGER = ToolsLogger
    registerCommonTemplateVars(GameType.BEDROCK)
    registerBedrockTemplateVars()
}

fun generateDrops(drops: List<WeightedDrop>, seed: Int, blockId: String, generatedDrops: GeneratedDrops): Pair<List<BaseDrop>, GeneratedDrops> {
    var allGeneratedDrops = generatedDrops
    val newDropsList = drops.map {
        val (newDrop, newGeneratedDrops) = replaceNbtWithGeneratedDrops(it, seed, blockId, generatedDrops)
        allGeneratedDrops = newGeneratedDrops
        newDrop
    }
    return Pair(newDropsList, allGeneratedDrops)
}

class GenerateBedrockDrops: Subcommand("generate-bedrock-drops", "Generate JS script containing pre-processed and aggregated drops for use in Bedrock edition") {
    val blockId by option(ArgType.String, description = "Lucky Block ID, e.g. lucky_block_red").required()
    val inputFolder by argument(ArgType.String, description = "Input config folder").optional().default(".")
    val outputJSFile by option(ArgType.String, description = "Output generated JS file").default("serverScript.js")
    val outputStructuresFolder by option(ArgType.String, description = "Output folder for generated structures").default("structures")
    val seed by option(ArgType.Int, description = "Drop generation seed").default(0)

    override fun execute() {
        prepareToGenerateDrops()

        val resources = loadAddonResources(File(inputFolder))!!

        var generatedDrops = createEmptyGeneratedDrops()
        val (blockDrops, generatedDropsWithBlock) = generateDrops(resources.drops[resources.addon.ids.block]!!, seed, blockId, generatedDrops)
        generatedDrops = generatedDropsWithBlock

        val luckyStructs = resources.structures.mapNotNull { (k, v) ->
            if (v !is DropStructureResource) null
            else {
                val (drops, generatedDropsWithStruct) = generateDrops(
                    v.drops.map { WeightedDrop(it, "") },
                    seed,
                    blockId,
                    generatedDrops
                )
                generatedDrops = generatedDropsWithStruct
                k to luckyStructToString(v.defaultProps, drops)
            }
        }.toMap()

        val outputJS = """
const serverSystem = server.registerSystem(0, 0);

serverSystem.registerEventData("lucky:${blockId}_config", {
    "drops": `
${blockDrops.joinToString("\n") { dropToString(it).replace("`", "\\`") } }
    `,
    "structures": {
        ${luckyStructs.map { (k, v) -> """"$k": `
${v.joinToString("\n") { it.replace("`", "\\`") } }
        `,
    """.trimIndent()
    }.joinToString("\n")}
    },
    "luck": 0,
});
        """.trimIndent()

        File(outputJSFile).writeText(outputJS)

        generatedDrops.dropStructures.forEach { (k, v) ->
            writeNbtFile(File(outputStructuresFolder).resolve("${k}.mcstructure"), v, compressed=false, isLittleEndian=true)
        }
    }
}
