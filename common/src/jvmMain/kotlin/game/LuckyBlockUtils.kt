package mod.lucky.java.game

import mod.lucky.common.*
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.runRandomDrop
import mod.lucky.java.*

data class LuckyBlockEntityData(
    val customDrops: List<WeightedDrop>? = null,
    val luck: Int = 0,
) { companion object }

fun LuckyBlockEntityData.toAttr(): DictAttr {
    return DropContainer(customDrops, luck).toAttr()
}

fun LuckyBlockEntityData.writeToTag(tag: NBTTag) {
    DropContainer(customDrops, luck).writeToTag(tag)
}
fun LuckyBlockEntityData.Companion.readFromTag(tag: NBTTag): LuckyBlockEntityData {
    val dropContainer = DropContainer.readFromTag(tag)
    return LuckyBlockEntityData(dropContainer.customDrops, dropContainer.luck ?: 0)
}

fun onLuckyBlockBreak(
    block: Block,
    world: World,
    pos: BlockPos,
    player: PlayerEntity? = null,
    blockEntityData: LuckyBlockEntityData?,
    removedByRedstone: Boolean = false,
) {
    try {
        // check for doDropsOnCreativeMode
        val blockId = JAVA_GAME_API.getBlockId(block) ?: return
        val settings = LuckyRegistry.blockSettings[blockId]!!
        if (player != null && JAVA_GAME_API.isCreativeMode(player) && !settings.doDropsOnCreativeMode && !removedByRedstone) return

        val vecPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)

        // drop just the block when using silk touch
        if (player != null && JAVA_GAME_API.hasSilkTouch(player) && !removedByRedstone) {
            val stackData = blockEntityData?.let { LuckyItemStackData.fromBlockEntityData(blockEntityData).toAttr() }
            gameAPI.dropItem(
                world = world,
                pos = vecPos,
                id = blockId,
                nbt = stackData,
                components = null,
            )
            return
        }

        // get target player
        val targetPlayer = player
            ?: gameAPI.getNearestPlayer(world, vecPos)
            ?: return

        // run a randrom drop
        val context = DropContext(world = world, pos = vecPos, player = targetPlayer, sourceId = blockId)
        runRandomDrop(blockEntityData?.customDrops, blockEntityData?.luck ?: 0, context, showOutput = true)
    } catch (e: Exception) {
        gameAPI.logError("Error performing Lucky Block function", e)
    }
}
