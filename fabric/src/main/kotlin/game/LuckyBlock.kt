package mod.lucky.fabric.game

import mod.lucky.common.LuckyRegistry
import mod.lucky.fabric.FabricLuckyRegistry
import mod.lucky.fabric.isClientWorld
import mod.lucky.fabric.toVec3i
import mod.lucky.fabric.*
import mod.lucky.java.*
import mod.lucky.java.game.*
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.BlockView
import net.minecraft.world.WorldView

private fun onBreak(
    block: Block,
    world: World,
    player: PlayerEntity?,
    pos: BlockPos,
    removedByRedstone: Boolean = false,
) {
    if (isClientWorld(world)) return

    val blockEntityData = (world.getBlockEntity(pos) as? LuckyBlockEntity?)?.data
    world.removeBlock(pos, false)
    world.removeBlockEntity(pos)

    onLuckyBlockBreak(
        block = block,
        world = world,
        player = player,
        pos = toVec3i(pos),
        blockEntityData = blockEntityData,
        removedByRedstone = removedByRedstone,
    )
}

class LuckyBlock : BlockWithEntity(Settings.of(Material.WOOD, DyeColor.YELLOW)
    .sounds(BlockSoundGroup.STONE)
    .strength(0.2f, 6000000.0f)) {

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        neighborBlock: Block,
        neighborPos: BlockPos,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos, notify)
        if (world.isReceivingRedstonePower(pos)) {
            onBreak(this, world, null, pos, removedByRedstone = true)
        }
    }

    override fun afterBreak(
        world: World,
        player: PlayerEntity,
        pos: BlockPos,
        state: BlockState,
        blockEntity: BlockEntity?,
        stack: ItemStack
    ) {
        super.afterBreak(world, player, pos, state, blockEntity, stack)
        onBreak(this, world, player, pos)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hitResult: BlockHitResult
    ): ActionResult {
        val settings = LuckyRegistry.blockSettings[JAVA_GAME_API.getBlockId(this)]!!
        if (settings.doDropsOnRightClick) {
            onBreak(this, world, player, pos)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, player: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, player, itemStack)

        val blockEntity = world.getBlockEntity(pos) as LuckyBlockEntity
        itemStack.nbt?.let {
            blockEntity.data = LuckyBlockEntityData.readFromTag(it)
            blockEntity.markDirty()
        }

        if (world.isReceivingRedstonePower(pos))
            onBreak(this, world, null, pos, removedByRedstone = true)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return LuckyBlockEntity(pos, state)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }
}


class LuckyBlockEntity(
    blockPos: MCBlockPos,
    blockState: BlockState,
    var data: LuckyBlockEntityData = LuckyBlockEntityData()
) : BlockEntity(FabricLuckyRegistry.luckyBlockEntity, blockPos, blockState) {

    override fun readNbt(tag: CompoundTag) {
        super.readNbt(tag)
        data = LuckyBlockEntityData.readFromTag(tag)
    }

    override fun writeNbt(tag: CompoundTag) {
        super.writeNbt(tag)
        data.writeToTag(tag)
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket {
        return BlockEntityUpdateS2CPacket.create(this) { blockEntity ->
            JAVA_GAME_API.attrToNBT((blockEntity as LuckyBlockEntity).data.toAttr()) as CompoundTag
        }
    }
}
