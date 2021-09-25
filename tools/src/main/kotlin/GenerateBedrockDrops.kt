package mod.lucky.tools

import kotlinx.cli.*
import mod.lucky.common.*
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
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

fun getIDWithNamespace(id: String): String {
    return if (":" in id) id else "minecraft:$id"
}

fun createDropStructure(dropType: String, dropProps: DictAttr, random: Random): DictAttr {
    if ("id" !in dropProps) throw Exception("Drop '${dropProps}' is missing the required property 'id'")
    return when(dropType) {
        "item" -> dictAttrOf(
            "" to dictAttrOf(
                "format_version" to intAttrOf(1),
                "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
                "structure" to dictAttrOf(
                    "block_indices" to listAttrOf(ListAttr(), ListAttr()),
                    "palette" to DictAttr(),
                    "entities" to listAttrOf(
                        dictAttrOf(
                            "Pos" to listAttrOf(
                                floatAttrOf((0.5 + (random.randDouble(0.0, 1.0) - 0.5)).toFloat()),
                                floatAttrOf(0.5f),
                                floatAttrOf((0.5 + (random.randDouble(0.0, 1.0) - 0.5)).toFloat()),
                            ),
                            "identifier" to stringAttrOf("minecraft:item"),
                            "Item" to dictAttrOf(
                                "Name" to stringAttrOf(getIDWithNamespace(dropProps.getValue("id"))),
                                "Count" to ValueAttr(AttrType.BYTE, 1.toByte()),
                                "Damage" to ValueAttr(AttrType.SHORT, dropProps.getWithDefault("data", 0).toShort()),
                                *(if ("nbttag" in dropProps) arrayOf("tag" to dropProps.getDict("nbttag")) else emptyArray()),
                            ),
                        ),
                    ),
                ),
                "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0))
            )
        )
        "entity" -> dictAttrOf(
            "" to dictAttrOf(
                "format_version" to intAttrOf(1),
                "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
                "structure" to dictAttrOf(
                    "block_indices" to listAttrOf(ListAttr(), ListAttr()),
                    "palette" to DictAttr(),
                    "entities" to listAttrOf(
                        dictAttrOf(
                            "Pos" to listAttrOf(floatAttrOf(0.5f), floatAttrOf(0f), floatAttrOf(0.5f)),
                            "identifier" to stringAttrOf(getIDWithNamespace(dropProps.getValue("id"))),
                        ).with(dropProps.getDict("nbttag").children)
                    ),
                ),
                "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0))
            )
        )
        "block" -> dictAttrOf(
            "" to dictAttrOf(
                "format_version" to intAttrOf(1),
                "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
                "structure" to dictAttrOf(
                    "block_indices" to listAttrOf(listAttrOf(intAttrOf(0)), listAttrOf(intAttrOf(-1))),
                    "palette" to dictAttrOf(
                        "default" to dictAttrOf(
                            "block_palette" to listAttrOf(
                                dictAttrOf(
                                    "name" to stringAttrOf(getIDWithNamespace(dropProps.getValue("id"))),
                                    "states" to dropProps.getWithDefault("state",  DictAttr()),
                                    "version" to intAttrOf(17825806),
                                ),
                            ),
                            "block_position_data" to dictAttrOf(
                                "0" to dictAttrOf(
                                    "block_entity_data" to dropProps.getDict("nbttag").withDefaults(mapOf(
                                        "x" to intAttrOf(0),
                                        "y" to intAttrOf(0),
                                        "z" to intAttrOf(0),
                                    )),
                                ),
                            ),
                        ),
                    ),
                    "entities" to listAttrOf(),
                ),
                "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0))
            )
        )
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

    val dropProps = dictAttrOf(
        "type" to stringAttrOf(drop.type),
        "id" to drop.props["id"],
        "samples" to intAttrOf(dropSamples),
        "seed" to intAttrOf(dropSeed),
        "nbttag" to drop.props["nbttag"],
        *(if (drop.type == "item" && "data" in drop.props) arrayOf("data" to drop.props["data"]) else emptyArray()),
        *(if (drop.type == "block") arrayOf("state" to drop.props["state"]) else emptyArray()),
    )
    val cacheKey = attrToSerializedString(dropProps)

    val hasCached = cacheKey in generatedDrops.dropStructureCache
    val (structureIds, newGeneratedDrops) =
        if (hasCached) Pair(generatedDrops.dropStructureCache[cacheKey]!!, generatedDrops)
        else {
            val seededRandom = SeededRandom(dropSeed)
            val spyRandom = SpyRandom(seededRandom)

            val evalContext = EvalContext(
                templateVarFns = LuckyRegistry.templateVarFns,
                templateContext = DropTemplateContext(drop = drop, dropContext = null, random = spyRandom),
            )

            val firstStructure = try {
                createDropStructure(drop.type, evalAttr(dropProps, evalContext) as DictAttr, seededRandom)
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

                    k to if (i == 0) firstStructure else
                        createDropStructure(drop.type, evalAttr(dropProps, evalContext) as DictAttr, seededRandom)
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

    val newDrop = SingleDrop(
        type = "structure",
        props = DictAttr(drop.props.children.plus(mapOf(
            "type" to stringAttrOf("structure"),
            "id" to structureIdAttr,
        )).minus(listOf("nbttag", "samples", "seed"))),
    )
    return Pair(newDrop, newGeneratedDrops)
}

fun <T : BaseDrop> replaceNBTWithGeneratedDrops(drop: T, seed: Int, blockId: String, generatedDrops: GeneratedDrops): Pair<T, GeneratedDrops> {
    @Suppress("UNCHECKED_CAST")
    return when (drop) {
        is WeightedDrop -> {
            val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(drop.drop, seed, blockId, generatedDrops)
            Pair(drop.copy(drop = newDrop) as T, newGeneratedDrops)
        }
        is GroupDrop -> {
            var allGeneratedDrops = generatedDrops
            val newDrops = drop.drops.map {
                val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(it, seed, blockId, generatedDrops)
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

fun generateDrops(drops: List<WeightedDrop>, seed: Int, blockId: String, generatedDrops: GeneratedDrops): Pair<List<BaseDrop>, GeneratedDrops> {
    var allGeneratedDrops = generatedDrops
    val newDropsList = drops.map {
        val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(it, seed, blockId, generatedDrops)
        allGeneratedDrops = newGeneratedDrops
        newDrop
    }
    return Pair(newDropsList, allGeneratedDrops)
}

fun main(args: Array<String>) {
    val parser = ArgParser("generate_bedrock_drops")
    val blockId by parser.option(ArgType.String, description = "Lucky Block ID, e.g. lucky_block_red").required()
    val inputFolder by parser.argument(ArgType.String, description = "Input config folder").optional().default(".")
    val outputJSFile by parser.option(ArgType.String, description = "Output generated JS file").default("serverScript.js")
    val outputStructuresFolder by parser.option(ArgType.String, description = "Output generated structures folder").default("structures")
    val seed by parser.option(ArgType.Int, description = "Drop generation seed").default(0)
    parser.parse(args)

    gameAPI = BedrockToolsGameAPI
    logger = ToolsLogger
    registerCommonTemplateVars(GameType.BEDROCK)
    registerBedrockTemplateVars()

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

    File(outputStructuresFolder).listFiles()?.forEach { it.delete() }
    generatedDrops.dropStructures.forEach { (k, v) ->
        val nbtBuffer = attrToNBT(v, ByteOrder.LITTLE_ENDIAN)
        writeBufferToFile(nbtBuffer, File(outputStructuresFolder).resolve("${k}.mcstructure"))
    }
}
