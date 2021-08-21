package mod.lucky.tools

import kotlinx.cli.*
import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.BaseDrop
import mod.lucky.common.drop.GroupDrop
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.java.loader.DropStructureResource
import mod.lucky.java.loader.loadAddonResources
import mod.lucky.java.loader.loadMainResources
import mod.lucky.java.registerJavaTemplateVars
import mod.lucky.java.javaGameAPI
import java.nio.ByteOrder
import java.io.File

data class GeneratedDrops(
    val blockId: String,
    val dropStructures: HashMap<String, DictAttr>,
    // (nbt string, seed, num instances) -> List<structreId>
    val dropStructureCache: HashMap<Triple<String, Int, Int>, List<String>>
)

class SpySeededRandom(
    seed: Int,
    private var wasUsed: Boolean = false,
    private val random: kotlin.random.Random = kotlin.random.Random(seed),
) : Random {
    override fun randInt(range: IntRange): Int {
        wasUsed = true
        return range.random(random)
    }

    override fun nextDouble(): Double {
        wasUsed = true
        return random.nextDouble()
    }

    fun wasUsed(): Boolean = wasUsed
}

object ToolsLogger : Logger {
    override fun logError(msg: String?, error: Exception?) {
        if (msg != null) println(error)
        if (error != null) println(error)
    }

    override fun logInfo(msg: String) {
        println(msg)
    }
}

fun getIDWithNamespace(id: String): String {
    return if (":" in id) id else "minecraft:$id"
}

fun createDropStructre(type: String, id: String, nbtAttr: DictAttr): DictAttr {
    return when(type) {
        "entity" -> dictAttrOf(
            "" to dictAttrOf(
                "format_version" to intAttrOf(1),
                "size" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)),
                "structure" to dictAttrOf(
                    "block_indices" to listAttrOf(ListAttr(), ListAttr()),
                    "palette" to DictAttr(),
                    "entities" to listAttrOf(
                        nbtAttr.with(mapOf(
                            "Pos" to listAttrOf(floatAttrOf(0.5f), floatAttrOf(0f), floatAttrOf(0.5f)),
                            "identifier" to stringAttrOf(getIDWithNamespace(id)),
                        )),
                    ),
                ),
                "structure_world_origin" to listAttrOf(intAttrOf(0), intAttrOf(0), intAttrOf(0))
            )
        )
        else -> dictAttrOf("x" to listAttrOf(intAttrOf(1), intAttrOf(1), intAttrOf(1)))
        //else -> DictAttr()
    }
}

fun generateSingleDrop(drop: SingleDrop, seed: Int, generatedDrops: GeneratedDrops): Pair<SingleDrop, GeneratedDrops> {
    val nbtAttrKey = when {
        drop.type == "entity" && "nbttag" in drop.props -> "nbttag"
        else -> null
    }
    val nbtAttr = nbtAttrKey?.let { drop.props[it] } ?: return Pair(drop, generatedDrops)
    val nbtAttrString = attrToSerializedString(nbtAttr)

    val dropSample = drop.props.getWithDefault("sample", 2)
    val dropSeed = drop.props.getWithDefault("seed", seed)

    val cacheKey = Triple(nbtAttrString, dropSeed, dropSample)
    val hasCached = cacheKey in generatedDrops.dropStructureCache
    val (structureIds, newGeneratedDrops) =
        if (hasCached) Pair(generatedDrops.dropStructureCache[cacheKey]!!, generatedDrops)
        else {
            val random = SpySeededRandom(dropSeed)
            val evalContext = EvalContext(
                templateVarFns = LuckyRegistry.templateVarFns,
                templateContext = DropTemplateContext(drop = drop, dropContext = null, random = random),
            )

            val firstStructure = createDropStructre(drop.type, drop["id"], evalAttr(nbtAttr, evalContext) as DictAttr)

            val newStructures: List<Pair<String, DictAttr>> = if (random.wasUsed()) {
                (0 until dropSample).mapIndexed { i, it ->
                    val k = "drop_" +
                        "${generatedDrops.dropStructureCache.size + 1}" +
                        if (dropSample > 1) ".${it + 1}" else ""

                    k to if (i == 0) firstStructure else
                        createDropStructre(drop.type, drop["id"], evalAttr(nbtAttr, evalContext) as DictAttr)
                }
            } else {
                listOf("drop_${generatedDrops.dropStructureCache.size + 1}" to firstStructure)
            }

            val newStructureIds = newStructures.map { it.first }
            generatedDrops.dropStructureCache[cacheKey] = newStructureIds
            generatedDrops.dropStructures.putAll(newStructures)
            Pair(newStructureIds, generatedDrops)
        }

    val structureIdAttr = if (structureIds.size == 1) {
        stringAttrOf(structureIds.first())
    } else {
        val templateVar = TemplateVar("randList", ListAttr(structureIds.map { stringAttrOf(it) }))
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
        )).minus(listOf("nbttag", "sample", "seed"))),
    )
    return Pair(newDrop, newGeneratedDrops)
}

fun <T : BaseDrop> replaceNBTWithGeneratedDrops(drop: T, seed: Int, generatedDrops: GeneratedDrops): Pair<T, GeneratedDrops> {
    @Suppress("UNCHECKED_CAST")
    return when (drop) {
        is WeightedDrop -> {
            val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(drop.drop, seed, generatedDrops)
            Pair(drop.copy(drop = newDrop) as T, newGeneratedDrops)
        }
        is GroupDrop -> {
            var allGeneratedDrops = generatedDrops
            val newDrops = drop.drops.map {
                val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(it, seed, generatedDrops)
                allGeneratedDrops = newGeneratedDrops
                newDrop
            }
            Pair(drop.copy(drops = newDrops) as T, allGeneratedDrops)
        }
        is SingleDrop -> generateSingleDrop(drop, seed, generatedDrops) as Pair<T, GeneratedDrops>
        else -> throw Exception()
    }
}

fun generateDrop(drop: WeightedDrop, seed: Int, generatedDrops: GeneratedDrops): Pair<String, GeneratedDrops> {
    val (newDrop, newGeneratedDrops) = replaceNBTWithGeneratedDrops(drop, seed, generatedDrops)
    return Pair(dropToString(newDrop), newGeneratedDrops)
}

fun createEmptyGeneratedDrops(blockId: String): GeneratedDrops {
    return GeneratedDrops(
        blockId = blockId,
        dropStructures = HashMap(),
        dropStructureCache = HashMap(),
    )
}

fun generateDrops(drops: List<WeightedDrop>, seed: Int, generatedDrops: GeneratedDrops): Pair<List<String>, GeneratedDrops> {
    var allGeneratedDrops = generatedDrops
    val dropsStrList = drops.map {
        val (newDropStr, newGeneratedDrops) = generateDrop(it, seed, generatedDrops)
        allGeneratedDrops = newGeneratedDrops
        newDropStr
    }
    return Pair(dropsStrList, allGeneratedDrops)
}

fun main(args: Array<String>) {
    val parser = ArgParser("generate_bedrock_drops")
    val blockId by parser.option(ArgType.String, description = "Lucky Block ID, e.g. lucky_block_red").required()
    val inputFolder by parser.argument(ArgType.String, description = "Input config folder").optional().default(".")
    val outputJSFile by parser.option(ArgType.String, description = "Output generated JS file").default("serverScript.js")
    val outputStructuresFolder by parser.option(ArgType.String, description = "Output generated structures folder").default("structures")
    val seed by parser.option(ArgType.Int, description = "Drop generation seed").default(0)
    parser.parse(args)

    javaGameAPI = ToolsJavaGameAPI
    logger = ToolsLogger
    registerJavaTemplateVars()

    val resources = loadAddonResources(File(inputFolder))!!

    var generatedDrops = createEmptyGeneratedDrops(blockId)
    val (blockDrops, generatedDropsWithBlock) = generateDrops(resources.drops[resources.addon.ids.block]!!, seed, generatedDrops)
    generatedDrops = generatedDropsWithBlock

    val luckyStructs = resources.structures.mapNotNull { (k, v) ->
        if (v !is DropStructureResource) null
        else {
            println(v.drops)
            val (drops, generatedDropsWithStruct) = generateDrops(resources.drops[resources.addon.ids.block]!!, seed, generatedDrops)
            generatedDrops = generatedDropsWithStruct
            k to drops
        }
    }.toMap()

    val outputJS = """
        const serverSystem = server.registerSystem(0, 0);
        
        serverSystem.registerEventData("lucky:${blockId}_config", {
            "drops": `
${blockDrops.joinToString("\n") { it.replace("`", "\\`") } }
            `,
            "structures": {
                ${luckyStructs.map { (k, v) -> """"$k": `
${v.joinToString("\n") { it.replace("`", "\\`") } }
                `,
            """
            }.joinToString("\n")}
            },
            "luck": 0,
        });
    """.trimIndent()

    File(outputJSFile).writeText(outputJS)
    generatedDrops.dropStructures.forEach { (k, v) ->
        val nbtBuffer = attrToNBT(v, ByteOrder.LITTLE_ENDIAN)
        writeBufferToFile(nbtBuffer, File(outputStructuresFolder).resolve("${k}.mcstructure"))
    }
}
