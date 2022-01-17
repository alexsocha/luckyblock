package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.*
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.onImpact
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.writeToTag
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.network.Packet
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class ThrownLuckyPotion : ThrownItemEntity {
    private var data: ThrownLuckyPotionData

    constructor(
        type: EntityType<ThrownLuckyPotion> = FabricLuckyRegistry.thrownLuckyPotion,
        world: World,
        data: ThrownLuckyPotionData = ThrownLuckyPotionData(),
    ) : super(type, world) {
        this.data = data
    }

    constructor(
        world: World,
        user: LivingEntity,
        data: ThrownLuckyPotionData
    ) : super(FabricLuckyRegistry.thrownLuckyPotion, user, world) {
        this.data = data
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (hitResult.type != HitResult.Type.MISS) {
            if (!isClientWorld(world)) {
                val hitEntity: Entity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(world, this, owner, hitEntity)
            }
            remove(RemovalReason.DISCARDED)
        }
    }

    override fun readCustomDataFromNbt(tag: CompoundTag) {
        (JAVA_GAME_API.readNBTKey(tag, "itemLuckyPotion") as? CompoundTag?)?.let {
            JAVA_GAME_API.writeNBTKey(tag, "Item", it)
        }
        super.readCustomDataFromNbt(tag)
        data = ThrownLuckyPotionData.readFromTag(tag)
    }

    override fun writeCustomDataToNbt(tag: CompoundTag) {
        super.writeCustomDataToNbt(tag)
        data.writeToTag(tag)
    }

    override fun getGravity(): Float {
        return 0.05f
    }

    override fun getDefaultItem(): Item {
        return FabricLuckyRegistry.luckyPotion
    }

    override fun createSpawnPacket(): Packet<*> {
        return SpawnPacket.fromEntity(this).toPacket()
    }
}

@OnlyInClient
class ThrownLuckyPotionRenderer(ctx: EntityRendererFactory.Context) :
    FlyingItemEntityRenderer<ThrownLuckyPotion>(ctx)
