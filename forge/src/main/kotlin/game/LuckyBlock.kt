package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.*
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.DyeColor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

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

class LuckyBlock : ContainerBlock(Properties.of(Material.WOOD, DyeColor.YELLOW)
    .sound(SoundType.STONE)
    .strength(0.2f, 6000000.0f)) {

    override fun neighborChanged(
        state: BlockState,
        world: World,
        pos: BlockPos,
        neighborBlock: Block,
        neighborPos: BlockPos,
        notify: Boolean
    ) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, notify)
        if (world.hasNeighborSignal(pos)) {
            onBreak(this, world, null, pos, removedByRedstone = true)
        }
    }

    override fun playerDestroy(
        world: World,
        player: PlayerEntity,
        pos: BlockPos,
        state: BlockState,
        blockEntity: TileEntity?,
        stack: ItemStack
    ) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack)
        onBreak(this, world, player, pos)
    }

    override fun setPlacedBy(world: World, pos: BlockPos, state: BlockState, player: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, pos, state, player, itemStack)

        val blockEntity = world.getBlockEntity(pos) as LuckyBlockEntity
        itemStack.tag?.let {
            blockEntity.data = LuckyBlockEntityData.readFromTag(it)
            blockEntity.setChanged()
        }

        if (world.hasNeighborSignal(pos))
            onBreak(this, world, null, pos, removedByRedstone = true)
    }

    override fun newBlockEntity(world: IBlockReader): TileEntity {
        return LuckyBlockEntity()
    }

    override fun getRenderShape(blockState: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }
}


class LuckyBlockEntity(
    var data: LuckyBlockEntityData = LuckyBlockEntityData()
) : TileEntity(ForgeLuckyRegistry.luckyBlockEntity) {

    override fun load(state: BlockState, tag: CompoundNBT) {
        super.load(state, tag)
        data = LuckyBlockEntityData.readFromTag(tag)
    }

    override fun save(tag: CompoundTag): CompoundTag {
        super.save(tag)
        data.writeToTag(tag)
        return tag
    }

    override fun getUpdatePacket(): SUpdateTileEntityPacket {
        return SUpdateTileEntityPacket(
            MCBlockPos(blockPos.x, blockPos.y, blockPos.z),
            1, // block entity type
            javaGameAPI.attrToNBT(data.toAttr()) as CompoundTag
        )
    }
}
