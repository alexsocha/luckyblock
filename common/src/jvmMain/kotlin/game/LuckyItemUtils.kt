package mod.lucky.java.game

import mod.lucky.common.Entity
import mod.lucky.common.PlayerEntity
import mod.lucky.common.World
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.runRandomDrop
import mod.lucky.common.gameAPI
import mod.lucky.java.*

object LuckyItemValues {
    const val veryLuckyBlock = "block.lucky.lucky_block.veryLucky"
    const val veryUnluckyBlock = "block.lucky.lucky_block.unlucky"
    const val veryLuckyPotion = "item.lucky.lucky_potion.veryLucky"
    const val veryUnluckyPotion = "item.lucky.lucky_potion.unlucky"
}

data class LuckyItemStackData(
    val customDrops: List<WeightedDrop>? = null,
    val luck: Int = 0,
) { companion object }

fun LuckyItemStackData.toAttr(): DictAttr {
    return DropContainer(customDrops, luck).toAttr()
}

fun LuckyItemStackData.Companion.readFromTag(tag: NBTTag): LuckyItemStackData {
    val dropContainer = DropContainer.readFromTag(tag)
    return LuckyItemStackData(dropContainer.customDrops, dropContainer.luck ?: 0)
}

fun LuckyItemStackData.Companion.fromBlockEntityData(entityData: LuckyBlockEntityData): LuckyItemStackData {
    return LuckyItemStackData(entityData.customDrops, entityData.luck)
}

fun doBowDrop(world: World, player: PlayerEntity, power: Double, stackNBT: NBTTag?, sourceId: String?) {
    val stackData = stackNBT?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()
    val (pos, _) = javaGameAPI.getArrowPosAndVelocity(world, player, power)

    runRandomDrop(
        stackData.customDrops,
        stackData.luck,
        context = DropContext(
            world = world,
            player = player,
            pos = pos,
            bowPower = power,
            sourceId = sourceId ?: JavaLuckyRegistry.bowId
        ),
        showOutput = true,
    )
}

fun doSwordDrop(world: World, player: PlayerEntity, hitEntity: Entity, stackNBT: NBTTag?, sourceId: String?) {
    val stackData = stackNBT?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()
    runRandomDrop(
        stackData.customDrops,
        stackData.luck,
        context = DropContext(
            world = world,
            player = player,
            hitEntity = hitEntity,
            pos = gameAPI.getEntityPos(hitEntity),
            sourceId = sourceId ?: JavaLuckyRegistry.swordId
        ),
        showOutput = true,
    )
}
