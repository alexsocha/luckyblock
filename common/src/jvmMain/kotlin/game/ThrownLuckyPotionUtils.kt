package mod.lucky.java.game

import mod.lucky.common.World
import mod.lucky.common.Entity
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.runRandomDrop
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.java.*

data class ThrownLuckyPotionData(
    val customDrops: List<WeightedDrop>? = null,
    val luck: Int = 0,
    val sourceId: String = JavaLuckyRegistry.potionId,
) {
    companion object {
        val attrKeys = listOf("drops", "luck", "sourceId")
    }
}

fun ThrownLuckyPotionData.onImpact(world: World, thrownPotionEntity: Entity, user: Entity?, hitEntity: Entity?) {
    try {
        val context = DropContext(
            world = world,
            player = user,
            hitEntity = hitEntity,
            pos = if (hitEntity != null) GAME_API.getEntityPos(hitEntity) else GAME_API.getEntityPos(thrownPotionEntity),
            sourceId = sourceId,
        )
        runRandomDrop(customDrops, luck = luck, context = context, showOutput = true)
    } catch (e: Exception) {
        LOGGER.logError("Error in lucky_potion impact", e)
    }
}

fun ThrownLuckyPotionData.toAttr(): DictAttr {
    return dictAttrOf(
        "drops" to customDrops?.let { dropsToAttrList(customDrops) },
        "luck" to intAttrOf(luck),
        "sourceId" to stringAttrOf(sourceId),
    )
}

fun ThrownLuckyPotionData.Companion.fromAttr(attr: DictAttr): ThrownLuckyPotionData {
    return try {
        ThrownLuckyPotionData(
            customDrops = ((attr["impact"] as DictAttr?)?.getList("drops"))?.let { dropsFromAttrList(it) },
            luck = attr.getOptionalValue("luck") ?: 0,
            sourceId = attr.getOptionalValue<String>("sourceId") ?: JavaLuckyRegistry.potionId,
        )
    } catch (e: java.lang.Exception) {
        LOGGER.logError("Error loading lucky_potion", e)
        ThrownLuckyPotionData()
    }
}

fun ThrownLuckyPotionData.writeToTag(tag: NBTTag) {
    writeNBTKeys(tag, toAttr())
}
fun ThrownLuckyPotionData.Companion.readFromTag(tag: NBTTag): ThrownLuckyPotionData {
    return fromAttr(readNBTKeys(tag, attrKeys))
}
