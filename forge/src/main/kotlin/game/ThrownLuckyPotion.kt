package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.onImpact
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.writeToTag
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ThrownItemRenderer

import net.minecraft.network.protocol.Packet
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraftforge.fmllegacy.network.NetworkHooks

class ThrownLuckyPotion : ThrowableItemProjectile, ItemSupplier {
    private var data: ThrownLuckyPotionData

    constructor(
        type: EntityType<ThrownLuckyPotion> = ForgeLuckyRegistry.thrownLuckyPotion,
        world: MCWorld,
        data: ThrownLuckyPotionData = ThrownLuckyPotionData(),
    ) : super(type, world) {
        this.data = data
    }

    constructor(
        world: MCWorld,
        user: LivingEntity,
        data: ThrownLuckyPotionData,
    ) : super(ForgeLuckyRegistry.thrownLuckyPotion, user, world) {
        this.data = data
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (hitResult.type != HitResult.Type.MISS) {
            if (!isClientWorld(level)) {
                val hitEntity: MCEntity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(level, this, owner, hitEntity)
            }
            remove(RemovalReason.DISCARDED)
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        (JAVA_GAME_API.readNBTKey(tag, "itemLuckyPotion") as? CompoundTag?)?.let {
            JAVA_GAME_API.writeNBTKey(tag, "Item", it)
        }
        super.readAdditionalSaveData(tag)
        data = ThrownLuckyPotionData.readFromTag(tag)
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        data.writeToTag(tag)
    }

    override fun getGravity(): Float {
        return 0.05f
    }

    override fun getDefaultItem(): MCItem {
        return ForgeLuckyRegistry.luckyPotion
    }

    override fun getAddEntityPacket(): Packet<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class ThrownLuckyPotionRenderer(ctx: EntityRendererProvider.Context) :
    ThrownItemRenderer<ThrownLuckyPotion>(ctx)
