package mod.lucky.java.game

import mod.lucky.common.Vec3d
import mod.lucky.common.World
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.intAttrOf
import mod.lucky.common.GAME_API
import mod.lucky.common.attribute.stringAttrOf
import mod.lucky.common.drop.*
import mod.lucky.java.*

data class DelayedDropData(
    val singleDropString: String? = null,
    val singleDrop: SingleDrop? = null,
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
                val player = context.player ?: context.playerUUID?.let { JAVA_GAME_API.findEntityByUUID(world, it)}
                val hitEntity = context.hitEntity ?: context.hitEntityUUID?.let { JAVA_GAME_API.findEntityByUUID(world, it)}
                val parsedDrop = singleDrop ?: singleDropString?.let { SingleDrop.fromString(it) }
                parsedDrop?.let { runDropAfterDelay(it, context.copy(world = world, player = player, hitEntity = hitEntity)) }
            }
        }
    } catch (e: Exception) {
        GAME_API.logError("Error running delayed drop: $singleDrop", e)
    }
}

fun DelayedDropData.toAttr(): DictAttr {
    return dictAttrOf(
        "drop" to stringAttrOf(singleDropString ?: ""),
        "context" to context.toAttr(),
        "ticksRemaining" to intAttrOf(ticksRemaining),
    )
}

fun DelayedDropData.Companion.fromAttr(attr: DictAttr, world: World): DelayedDropData {
    return try {
        DelayedDropData(
            singleDropString=attr.getValue("drop"),
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
