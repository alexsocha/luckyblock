package mod.lucky.fabric.game

import mod.lucky.common.LuckyRegistry
import mod.lucky.fabric.isClientWorld
import mod.lucky.fabric.toVec3i
import mod.lucky.fabric.*
import mod.lucky.java.JAVA_GAME_API
import mod.lucky.java.game.*
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult

private fun onBreak(
    block: MCBlock,
    world: MCWorld,
    player: MCPlayerEntity?,
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

class LuckyBlock : BaseEntityBlock(Properties.of(Material.WOOD, DyeColor.YELLOW)
    .sound(SoundType.STONE)
    .strength(0.2f, 6000000.0f)) {

    override fun neighborChanged(
        state: BlockState,
        world: MCWorld,
        pos: MCBlockPos,
        neighborBlock: MCBlock,
        neighborPos: MCBlockPos,
        notify: Boolean
    ) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, notify)
        if (world.hasNeighborSignal(pos)) {
            onBreak(this, world, null, pos, removedByRedstone = true)
        }
    }

    override fun playerDestroy(
        world: MCWorld,
        player: MCPlayerEntity,
        pos: MCBlockPos,
        state: BlockState,
        blockEntity: BlockEntity?,
        stack: MCItemStack
    ) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack)
        onBreak(this, world, player, pos)
    }

    override fun use(
        blockState: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        val settings = LuckyRegistry.blockSettings[JAVA_GAME_API.getBlockId(this)]!!
        if (settings.doDropsOnRightClick) {
            onBreak(this, world, player, pos)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun setPlacedBy(world: MCWorld, pos: BlockPos, state: BlockState, player: LivingEntity?, itemStack: MCItemStack) {
        super.setPlacedBy(world, pos, state, player, itemStack)

        val blockEntity = world.getBlockEntity(pos) as LuckyBlockEntity
        itemStack.tag?.let {
            blockEntity.data = LuckyBlockEntityData.readFromTag(it)
            blockEntity.setChanged()
        }

        if (world.hasNeighborSignal(pos))
            onBreak(this, world, null, pos, removedByRedstone = true)
    }

    override fun newBlockEntity(pos: MCBlockPos, state: BlockState): BlockEntity {
        return LuckyBlockEntity(pos, state)
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }
}


class LuckyBlockEntity(
    blockPos: MCBlockPos,
    blockState: BlockState,
    var data: LuckyBlockEntityData = LuckyBlockEntityData()
) : BlockEntity(FabricLuckyRegistry.luckyBlockEntity, blockPos, blockState) {

    override fun load(tag: CompoundTag) {
        super.load(tag)
        data = LuckyBlockEntityData.readFromTag(tag)
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        data.writeToTag(tag)
    }

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket {
        return ClientboundBlockEntityDataPacket.create(this) { blockEntity ->
            JAVA_GAME_API.attrToNBT((blockEntity as LuckyBlockEntity).data.toAttr()) as CompoundTag
        }
    }
}
