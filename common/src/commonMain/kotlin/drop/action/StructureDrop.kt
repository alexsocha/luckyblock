package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*
import kotlin.math.floor

fun resolveStructureId(id: String, sourceId: String): String {
    return when (id.split(':').size) {
        2 -> "lucky:$id"
        1 -> {
            val fullId = LuckyRegistry.sourceToAddonId[sourceId]?.let { "$it:$id" }
            if (fullId != null && fullId in LuckyRegistry.structureProps) fullId
            else LuckyRegistry.structureProps.keys.firstOrNull { it.endsWith(":$id") } ?: id
        }
        else -> id
    }
}

fun doStructureDrop(drop: SingleDrop, context: DropContext) {
    val structureId =  resolveStructureId(drop["id"], context.sourceId)
    val defaultProps = LuckyRegistry.structureProps[structureId]

    if (defaultProps == null) {
        gameAPI.logError("Missing structure '$structureId'")
        return
    }

    val dropWithDefaults = drop.copy(props = drop.props.withDefaults(defaultProps.children))
    val rotation: Int = positiveMod(dropWithDefaults["rotation"], 4)

    val size = dropWithDefaults.getVec3<Double>("size")
    val centerOffset = dropWithDefaults.getVec3(
        "centerOffset",
        default = Vec3d(floor(size.x / 2.0), 0.0, floor(size.z / 2.0))
    )
    val centerOffsetInt = centerOffset.floor()

    val pos = drop.getPos(context.pos)
    val posInt = pos.floor()

    val mode: String = dropWithDefaults["blockMode"]
    val notify: Boolean = dropWithDefaults["blockUpdate"]

    if (mode == "replace") {
        val minPos = getWorldPos(Vec3d(0.0, 0.0, 0.0), centerOffsetInt.toDouble(), posInt.toDouble(), rotation).floor()
        val maxPos = getWorldPos(size - Vec3d(1.0, 1.0, 1.0), centerOffsetInt.toDouble(), posInt.toDouble(), rotation).floor()

        for (x in minPos.x..maxPos.x) {
            for (y in minPos.y..maxPos.y) {
                for (z in minPos.z..maxPos.z) {
                    gameAPI.setBlock(
                        world = context.world,
                        pos = Vec3i(x, y, z),
                        blockId = "minecraft:air",
                        state = null,
                        rotation = 0,
                        notify = notify,
                    )
                }
            }
        }
    }

    val structureDrops = LuckyRegistry.structureDrops[structureId]
    if (structureDrops != null) {
        val commonDefaults = mapOf(
            "pos" to vec3AttrOf(AttrType.DOUBLE, pos),
            "rotation" to intAttrOf(rotation),
            "centerOffset" to vec3AttrOf(AttrType.DOUBLE, centerOffset),
        )
        val blockDefaults = commonDefaults + mapOfNotNull(
            "blockMode" to stringAttrOf(mode),
            "blockUpdate" to booleanAttrOf(notify),
        )

        fun mergeDefaultDropProps(drop: BaseDrop): BaseDrop {
            return when (drop) {
                is SingleDrop -> drop.copy(props=drop.props.withDefaults(
                    if (drop.type == "block") blockDefaults else commonDefaults
                )),
                is GroupDrop -> drop.copy(drops=drop.drops.map { mergeDefaultDropProps(it) })
                else -> throw Exception()
            }
        }

        for (structureDrop in structureDrops) {
            val evaluatedDrops = evalDrop(mergeDefaultDropProps(structureDrop), context)
            evaluatedDrops.forEach { runEvaluatedDrop(it, context) }
        }
    } else {
        gameAPI.createStructure(
            world = context.world,
            structureId = structureId,
            pos = posInt,
            centerOffset = centerOffsetInt,
            rotation = rotation,
            mode = mode,
            notify = notify,
        )
    }

    // compatibility
    if ("overlayStruct" in dropWithDefaults) {
        val overlayDrop = SingleDrop("structure", dictAttrOf(
            "id" to dropWithDefaults.props["overlayStruct"]!!,
            "blockMode" to stringAttrOf("overlay"),
            "rotation" to intAttrOf(rotation),
            "pos" to vec3AttrOf(AttrType.DOUBLE, pos),
        ))
        doStructureDrop(overlayDrop, context)
    }
}
