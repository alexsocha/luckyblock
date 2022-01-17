package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*
import kotlin.math.floor

fun resolveStructureId(id: String, sourceId: String): String {
    // structure IDs should be in the form addonId:structureId, where addonId starts with lucky:
    val numParts = id.split(":").size
    if (numParts == 2 && !id.startsWith("lucky:")) return "lucky:$id"
    if (numParts <= 2) {
        val fullId = LuckyRegistry.sourceToAddonId[sourceId]?.let { "$it:${id.split(":").last()}" }
        if (fullId != null && fullId in LuckyRegistry.defaultStructureProps) return fullId

        // try to find an existing structure which matches the ID
        return LuckyRegistry.defaultStructureProps.keys.firstOrNull { it.endsWith(":$id") } ?: fullId ?: id
    }
    return id
}

data class StructureConfig(
    val structureId: String,
    val rotation: Int,
    val size: Vec3d,
    val pos: Vec3d,
    val centerOffset: Vec3d,
    val mode: String,
    val notify: Boolean,
    val overlayStructureId: String? // compatibility
)
fun getStructureConfig(drop: SingleDrop, context: DropContext): StructureConfig {
    val structureId = resolveStructureId(drop["id"], context.sourceId)
    val defaultProps = LuckyRegistry.defaultStructureProps[structureId] ?: DictAttr()

    val dropWithDefaults = drop.copy(props = drop.props.withDefaults(defaultProps.children))
    val rotation: Int = positiveMod(dropWithDefaults["rotation"], 4)

    val size = dropWithDefaults.getVec3<Double>("size")
    val centerOffset = dropWithDefaults.getVec3(
        "centerOffset",
        default = Vec3d(floor(size.x / 2.0), 0.0, floor(size.z / 2.0))
    )

    val pos = calculatePos(drop, context.pos, context.world)

    val mode: String = dropWithDefaults["blockMode"]
    val notify: Boolean = dropWithDefaults["blockUpdate"]

    return StructureConfig(
        structureId = structureId,
        rotation = rotation,
        size = size,
        pos = pos,
        centerOffset = centerOffset,
        mode = mode,
        notify = notify,
        dropWithDefaults.props.getOptionalValue("overlayStruct"),
    )
}

fun createDropStructure(dropStructure: DropStructure, dropContext: DropContext, structureConfig: StructureConfig) {
    val commonDefaults = mapOf(
        "pos" to vec3AttrOf(AttrType.DOUBLE, structureConfig.pos),
        "rotation" to intAttrOf(structureConfig.rotation),
        "centerOffset" to vec3AttrOf(AttrType.DOUBLE, structureConfig.centerOffset),
    )
    val blockDefaults = commonDefaults + mapOfNotNull(
        "blockMode" to stringAttrOf(structureConfig.mode),
        "blockUpdate" to booleanAttrOf(structureConfig.notify),
    )

    fun mergeDefaultDropProps(drop: BaseDrop): BaseDrop {
        return when (drop) {
            is SingleDrop -> drop.copy(props=drop.props.withDefaults(
                if (drop.type == "block") blockDefaults else commonDefaults
            ))
            is GroupDrop -> drop.copy(drops=drop.drops.map { mergeDefaultDropProps(it) })
            else -> throw Exception()
        }
    }

    for (drop in dropStructure.drops) {
        val evaluatedDrops = evalDrop(mergeDefaultDropProps(drop), createDropEvalContext(dropContext))
        evaluatedDrops.forEach { runEvaluatedDrop(it, dropContext) }
    }
}

fun doStructureDrop(drop: SingleDrop, dropContext: DropContext) {
    val structureConfig = getStructureConfig(drop, dropContext)

    if (structureConfig.mode == "replace") {
        val minPos = getWorldPos(
            Vec3d(0.0, 0.0, 0.0),
            structureConfig.centerOffset.floor().toDouble(),
            structureConfig.pos.floor().toDouble(),
            structureConfig.rotation
        ).floor()

        val maxPos = getWorldPos(
            structureConfig.size - Vec3d(1.0, 1.0, 1.0),
            structureConfig.centerOffset.floor().toDouble(),
            structureConfig.pos.floor().toDouble(),
            structureConfig.rotation
        ).floor()

        for (x in minPos.x..maxPos.x) {
            for (y in minPos.y..maxPos.y) {
                for (z in minPos.z..maxPos.z) {
                    GAME_API.setBlock(
                        world = dropContext.world,
                        pos = Vec3i(x, y, z),
                        id = "minecraft:air",
                        state = null,
                        components = null,
                        rotation = 0,
                        notify = structureConfig.notify,
                    )
                }
            }
        }
    }

    val dropStructure = LuckyRegistry.dropStructures.get(structureConfig.structureId)
    if (dropStructure != null) {
        createDropStructure(dropStructure, dropContext, structureConfig)
    } else {
        GAME_API.createStructure(
            world = dropContext.world,
            structureId = structureConfig.structureId,
            pos = structureConfig.pos.floor(),
            centerOffset = structureConfig.centerOffset.floor(),
            rotation = structureConfig.rotation,
            mode = structureConfig.mode,
            notify = structureConfig.notify,
        )
    }

    // compatibility
    if (structureConfig.overlayStructureId != null) {
        val overlayDrop = SingleDrop("structure", dictAttrOf(
            "id" to stringAttrOf(structureConfig.overlayStructureId),
            "blockMode" to stringAttrOf("overlay"),
            "rotation" to intAttrOf(structureConfig.rotation),
            "pos" to vec3AttrOf(AttrType.DOUBLE, structureConfig.pos),
        ))
        doStructureDrop(overlayDrop, dropContext)
    }
}
