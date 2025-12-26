package mod.lucky.neoforge.game

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.Codec
import mod.lucky.common.GAME_API
import mod.lucky.common.drop.dropsFromStrList
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.game.LuckyProjectileData
import mod.lucky.java.game.onImpact
import mod.lucky.java.game.tick
import mod.lucky.java.game.writeToTag
import mod.lucky.neoforge.*
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

private val defaultDisplayItemStack = MCItemStack(Items.STICK)

class LuckyProjectile(
    type: EntityType<LuckyProjectile> = ForgeLuckyRegistry.luckyProjectile.get(),
    world: MCWorld,
    private var data: LuckyProjectileData = LuckyProjectileData(),
) : Arrow(type, world) {
    companion object {
        val ITEM_STACK: EntityDataAccessor<MCItemStack> = SynchedEntityData.defineId(
            LuckyProjectile::class.java, EntityDataSerializers.ITEM_STACK
        )
    }

    override fun defineSynchedData(p0: SynchedEntityData.Builder) {
        super.defineSynchedData(p0)
        p0.define(ITEM_STACK, MCItemStack.EMPTY)
    }

    override fun tick() {
        super.tick()
        if (!isClientWorld(level())) data.tick(level(), this, owner, tickCount)
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (hitResult.type != HitResult.Type.MISS){
            if (!isClientWorld(level())) {
                val hitEntity: MCEntity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(level(), this, owner, hitEntity)
            }
            remove(RemovalReason.DISCARDED)
        }
    }
    override fun readAdditionalSaveData(tag: ValueInput) {
        super.readAdditionalSaveData(tag)

        data = LuckyProjectileData(
            trailFreqPerTick = tag.child("trail").getOrNull()?.getDoubleOr("frequency", 0.0) ?: 0.0,
            trailDrops = dropsFromStrList(tag.child("trail").getOrNull()?.listOrEmpty("drops", Codec.STRING)?.toList() ?: emptyList()),
            impactDrops = dropsFromStrList(tag.listOrEmpty("impact", Codec.STRING).toList()),
            sourceId = tag.getString("sourceId").getOrNull() ?: JavaLuckyRegistry.blockId,
        )

        val itemInput = tag.child("item").getOrNull() ?: tag.child("Item").getOrNull()
        val stack = itemInput?.let {
            val id = it.getString("id").getOrDefault("minecraft:invalid")
            val itemKey = MCIdentifier.parse(id)
            if (!BuiltInRegistries.ITEM.containsKey(itemKey)) {
                GAME_API.logError("Invalid item ID: '$id'")
                return
            }
            val item = BuiltInRegistries.ITEM.getOptional(itemKey).get()
            val stack = MCItemStack(item, 1)
            it.read("components", DataComponentMap.CODEC).getOrNull()?.let { stack.applyComponents(it) }
            stack.count = 1
            stack
        } ?: defaultDisplayItemStack
        entityData.set(ITEM_STACK, stack)
    }

    override fun addAdditionalSaveData(tag: ValueOutput) {
        super.addAdditionalSaveData(tag)
        val parentNbt = CompoundTag()

        data.writeToTag(parentNbt)
        tag.store(parentNbt)

        val stack = entityData.get(ITEM_STACK)
        val stackNbt = CompoundTag()
        stackNbt.putString("id", BuiltInRegistries.ITEM.getKeyOrNull(stack.item)?.path ?: "minecraft:air")
        stackNbt.put("components", componentsToNbt(stack.components, registryAccess()))
        parentNbt.put("Item", stackNbt)

        tag.store(parentNbt)
    }
}

@OnlyIn(Dist.CLIENT)
open class LuckyProjectileRenderState : EntityRenderState() {
    var itemEntity: ItemEntityRenderState? = null
}

@OnlyInClient
class LuckyProjectileRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<LuckyProjectile, LuckyProjectileRenderState>(
    ctx) {
    private var itemModelResolver = ctx.itemModelResolver;

    override fun render(
        renderState: LuckyProjectileRenderState,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        renderState.itemEntity?.let {
            try {
                entityRenderDispatcher.getRenderer(it).render(
                    it, poseStack, bufferSource, packedLight
                )
            } catch (e: Exception) {
                GAME_API.logError("Failed to render LuckyProjectile: ${e}")
            }
        }
    }

    override fun createRenderState(): LuckyProjectileRenderState {
        return LuckyProjectileRenderState()
    }

    override fun extractRenderState(
        entity: LuckyProjectile,
        renderState: LuckyProjectileRenderState,
        partialTick: Float
    ) {
        super.extractRenderState(entity, renderState, partialTick)
        val itemEntity = ItemEntityRenderState()
        val itemStack = entity.entityData.get(LuckyProjectile.ITEM_STACK)
        itemEntity.extractItemGroupRenderState(entity, itemStack, itemModelResolver)
        itemEntity.entityType = EntityType.ITEM
        renderState.itemEntity = itemEntity
    }
}
