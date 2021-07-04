package mod.lucky.forge.game

import com.mojang.blaze3d.matrix.MatrixStack
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.*
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.Items
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks

private val defaultDisplayItemStack = MCItemStack(Items.STICK)

class LuckyProjectile(
    type: EntityType<LuckyProjectile> = ForgeLuckyRegistry.luckyProjectile,
    world: World,
    private var data: LuckyProjectileData = LuckyProjectileData(),
) : ArrowEntity(type, world) {
    var itemEntity: ItemEntity? = null

    companion object {
        private val ITEM_STACK: DataParameter<MCItemStack> = EntityDataManager.defineId(
            LuckyProjectile::class.java, DataSerializers.ITEM_STACK
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

    override fun onHit(hitResult: RayTraceResult) {
        super.onHit(hitResult)
        if (hitResult.type != RayTraceResult.Type.MISS){
            if (!isClientWorld(level)) {
                val hitEntity: Entity? = (hitResult as? EntityRayTraceResult)?.entity
                data.onImpact(level, this, owner, hitEntity)
            }
            remove()
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

    override fun getAddEntityPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class LuckyProjectileRenderer(ctx: EntityRendererManager) : EntityRenderer<LuckyProjectile>(
    ctx) {
    override fun render(
        entity: LuckyProjectile,
        yawDeg: Float,
        particleTicks: Float,
        matrix: MatrixStack,
        vertexProvider: IRenderTypeBuffer,
        light: Int,
    ) {
        val itemEntity = entity.itemEntity ?: return
        dispatcher.getRenderer(itemEntity)?.render(
            itemEntity,
            yawDeg, particleTicks,
            matrix, vertexProvider, light
        )
    }

    override fun getTextureLocation(entity: LuckyProjectile): MCIdentifier? {
        return null
    }
}
