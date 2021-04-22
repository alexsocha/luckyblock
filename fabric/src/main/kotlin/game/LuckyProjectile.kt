package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.*
import mod.lucky.java.game.*
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

private val defaultDisplayItemStack = MCItemStack(Items.STICK)

class LuckyProjectile(
    type: EntityType<LuckyProjectile> = FabricLuckyRegistry.luckyProjectile,
    world: World,
    private var data: LuckyProjectileData = LuckyProjectileData(),
) : ArrowEntity(type, world) {
    var itemEntity: ItemEntity? = null

    companion object {
        private val ITEM_STACK: TrackedData<ItemStack> = DataTracker.registerData(
            LuckyProjectile::class.java, TrackedDataHandlerRegistry.ITEM_STACK
        )
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.getDataTracker().startTracking(ITEM_STACK, ItemStack.EMPTY)
    }

    override fun tick() {
        super.tick()

        if (this.itemEntity == null) {
            this.itemEntity = ItemEntity(
                this.entityWorld,
                pos.x, pos.y, pos.z,
                this.dataTracker.get(ITEM_STACK)
            )
        }
        itemEntity?.tick()

        if (!isClientWorld(world)) data.tick(world, this, owner, age)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (hitResult.type != HitResult.Type.MISS){
            if (!isClientWorld(world)) {
                val hitEntity: Entity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(world, this, owner, hitEntity)
            }
            remove()
        }
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        super.readCustomDataFromTag(tag)
        data = LuckyProjectileData.readFromTag(tag)
        val stackNBT = (javaGameAPI.readNBTKey(tag, "item") ?: javaGameAPI.readNBTKey(tag, "Item")) as? CompoundTag?
        val stack = stackNBT.let { MCItemStack.fromTag(it) } ?: defaultDisplayItemStack
        stack.count = 1
        stack.count = 1
        dataTracker.set(ITEM_STACK, stack)
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        super.writeCustomDataToTag(tag)
        data.writeToTag(tag)
        val stack = dataTracker.get(ITEM_STACK)
        javaGameAPI.writeNBTKey(tag, "Item", stack.toTag(CompoundTag()))
    }

    override fun createSpawnPacket(): Packet<*> {
        return SpawnPacket.fromEntity(this).toPacket()
    }
}

@OnlyInClient
class LuckyProjectileRenderer(dispatcher: EntityRenderDispatcher) : EntityRenderer<LuckyProjectile>(dispatcher) {
    override fun render(
        entity: LuckyProjectile,
        yawDeg: Float,
        particleTicks: Float,
        matrix: MatrixStack?,
        vertexProvider: VertexConsumerProvider?,
        light: Int,
    ) {
        val itemEntity = entity.itemEntity ?: return
        renderManager.getRenderer(itemEntity)?.render(
            itemEntity,
            yawDeg, particleTicks,
            matrix, vertexProvider, light
        )
    }

    override fun getTexture(entity: LuckyProjectile): Identifier? {
        return null
    }
}
