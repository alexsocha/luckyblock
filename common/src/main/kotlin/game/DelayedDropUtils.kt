package mod.lucky.java.game

import mod.lucky.common.Vec3d
import mod.lucky.common.World
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.intAttrOf
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.runDropAfterDelay
import mod.lucky.common.GAME_API
import mod.lucky.java.*

data class DelayedDropData(
    val singleDrop: SingleDrop,
    val context: DropContext,
    var ticksRemaining: Int,
) {
    companion object {
        val attrKeys = listOf("drop", "context", "ticksRemaining")
    }
}

fun DelayedDropData.tick(world: World) {
    try {
        if (ticksRemaining > 0) {
            ticksRemaining--
            if (ticksRemaining == 0) {
                runDropAfterDelay(singleDrop, context.copy(world = world))
            }
        }
    } catch (e: Exception) {
        GAME_API.logError("Error running delayed drop: $singleDrop", e)
    }
}

fun DelayedDropData.toAttr(): DictAttr {
    return dictAttrOf(
        "drop" to singleDrop.toAttr(),
        "context" to context.toAttr(),
        "ticksRemaining" to intAttrOf(ticksRemaining),
    )
}

fun DelayedDropData.Companion.fromAttr(attr: DictAttr, world: World): DelayedDropData {
    return try {
        DelayedDropData(
            singleDrop=SingleDrop.fromAttr(attr.getDict("drop")),
            context=DropContext.fromAttr(attr.getDict("context"), world),
            ticksRemaining=attr.getValue("ticksRemaining"),
        )
    } catch (e: java.lang.Exception) {
        GAME_API.logError("Error loading delayed drop", e)
        createDefault(world)
    }
}

fun DelayedDropData.Companion.createDefault(world: World): DelayedDropData {
    return DelayedDropData(
        singleDrop = SingleDrop.nothingDrop,
        context = DropContext(world, Vec3d(0.0, 0.0, 0.0), sourceId = JavaLuckyRegistry.blockId),
        ticksRemaining = 0
    )
}
fun DelayedDropData.writeToTag(tag: NBTTag) {
    writeNBTKeys(tag, toAttr())
}
fun DelayedDropData.Companion.readFromTag(tag: NBTTag, world: World): DelayedDropData {
    return DelayedDropData.fromAttr(readNBTKeys(tag, attrKeys), world)
}
