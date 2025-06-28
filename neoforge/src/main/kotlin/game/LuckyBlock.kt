package mod.lucky.forge.game

import com.mojang.serialization.MapCodec
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap

import mod.lucky.common.LuckyRegistry
import mod.lucky.java.JAVA_GAME_API
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.game.LuckyBlockEntityData
import mod.lucky.java.game.onLuckyBlockBreak
import mod.lucky.java.game.readFromTag
import mod.lucky.neoforge.*
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.redstone.Orientation
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.phys.BlockHitResult
import java.util.*
import kotlin.math.min

private fun onBreak(
    block: MCBlock,
    world: MCWorld,
    player: MCPlayerEntity?,
    pos: BlockPos,
    blockEntity: BlockEntity? = null,
    removedByRedstone: Boolean = false,
) {
    if (isClientWorld(world)) return

    val blockEntityWithDefault = blockEntity ?: world.getBlockEntity(pos)
    val blockEntityData = (blockEntityWithDefault as? LuckyBlockEntity?)?.data
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

class LuckyBlock(registryId: MCIdentifier) : BaseEntityBlock(Properties.of()
    .setId(ResourceKey.create(Registries.BLOCK, registryId))
    .mapColor(MapColor.COLOR_YELLOW)
    .sound(SoundType.STONE)
    .strength(0.2f, 6000000.0f)) {

    companion object {
        val CODEC = ForgeLuckyRegistry.blockTypeRegistry.register(
            MCIdentifier.parse(JavaLuckyRegistry.blockId).path,
            { id -> simpleCodec { _ -> LuckyBlock(id) } }
        )
    }
    override fun codec(): MapCodec<LuckyBlock> { return CODEC.value() }

    override fun neighborChanged(
        state: BlockState,
        world: MCWorld,
        pos: MCBlockPos,
        neighborBlock: Block,
        orientation: Orientation?,
        movedByPiston: Boolean
    ) {
        super.neighborChanged(state, world, pos, neighborBlock, orientation, movedByPiston)
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
        onBreak(this, world, player, pos, blockEntity)
    }

    override fun useWithoutItem(
        blockState: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
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
        itemStack.components.let {
            val nbt = componentsToNbt(itemStack.components, world.registryAccess())
            blockEntity.data = LuckyBlockEntityData.readFromTag(nbt)
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
    var data: LuckyBlockEntityData = LuckyBlockEntityData(),
) : BlockEntity(ForgeLuckyRegistry.luckyBlockEntity.get(), blockPos, blockState) {
    override fun onLoad() {
        super.onLoad()
        this.level?.let {
            val tag = componentsToNbt(components(), it.registryAccess())
            data = LuckyBlockEntityData.readFromTag(tag)
        }
    }
}
