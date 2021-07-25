package mod.lucky.forge.game

import com.mojang.blaze3d.vertex.PoseStack
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.*
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.network.protocol.Packet
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.Items
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraftforge.fmllegacy.network.NetworkHooks

private val defaultDisplayItemStack = MCItemStack(Items.STICK)

class LuckyProjectile(
    type: EntityType<LuckyProjectile> = ForgeLuckyRegistry.luckyProjectile,
    world: MCWorld,
    private var data: LuckyProjectileData = LuckyProjectileData(),
) : Arrow(type, world) {
    var itemEntity: ItemEntity? = null

    companion object {
        private val ITEM_STACK: EntityDataAccessor<MCItemStack> = SynchedEntityData.defineId(
            LuckyProjectile::class.java, EntityDataSerializers.ITEM_STACK
        )
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        this.entityData.define(ITEM_STACK, MCItemStack.EMPTY)
    }

    override fun tick() {
        super.tick()

        if (this.itemEntity == null) {
            this.itemEntity = ItemEntity(
                this.level,
                x, y, z,
                entityData.get(ITEM_STACK)
            )
        }
        itemEntity?.tick()

        if (!isClientWorld(level)) data.tick(level, this, owner, tickCount)
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (hitResult.type != HitResult.Type.MISS){
            if (!isClientWorld(level)) {
                val hitEntity: MCEntity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(level, this, owner, hitEntity)
            }
            remove(RemovalReason.DISCARDED)
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        data = LuckyProjectileData.readFromTag(tag)
        val stackNBT = (javaGameAPI.readNBTKey(tag, "item") ?: javaGameAPI.readNBTKey(tag, "Item")) as? CompoundTag?
        val stack = stackNBT?.let { MCItemStack.of(it) } ?: defaultDisplayItemStack
        stack.count = 1
        stack.count = 1
        entityData.set(ITEM_STACK, stack)
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        data.writeToTag(tag)
        val stack = entityData.get(ITEM_STACK)
        javaGameAPI.writeNBTKey(tag, "Item", stack.save(CompoundTag()))
    }

    override fun getAddEntityPacket(): Packet<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class LuckyProjectileRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<LuckyProjectile>(
    ctx) {
    override fun render(
        entity: LuckyProjectile,
        yawDeg: Float,
        particleTicks: Float,
        matrix: PoseStack,
        vertexProvider: MultiBufferSource,
        light: Int,
    ) {
        val itemEntity = entity.itemEntity ?: return
        entityRenderDispatcher.getRenderer(itemEntity)?.render(
            itemEntity,
            yawDeg, particleTicks,
            matrix, vertexProvider, light
        )
    }

    override fun getTextureLocation(entity: LuckyProjectile): MCIdentifier? {
        return null
    }
}
