package mod.lucky.neoforge.game

import com.mojang.serialization.Codec
import mod.lucky.common.GAME_API
import mod.lucky.common.drop.dropsFromStrList
import mod.lucky.neoforge.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyProjectileData
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.onImpact
import mod.lucky.java.game.writeToTag
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ThrownItemRenderer

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

class ThrownLuckyPotion : ThrowableItemProjectile {
    private var data: ThrownLuckyPotionData

    constructor(
        type: EntityType<ThrownLuckyPotion> = ForgeLuckyRegistry.thrownLuckyPotion.get(),
        world: MCWorld,
        data: ThrownLuckyPotionData = ThrownLuckyPotionData(),
    ) : super(type, world) {
        this.data = data
    }

    constructor(
        world: MCWorld,
        user: LivingEntity,
        data: ThrownLuckyPotionData,
        itemStack: MCItemStack,
        type: EntityType<ThrownLuckyPotion> = ForgeLuckyRegistry.thrownLuckyPotion.get(),
    ) : super(type, user, world, itemStack) {
        this.data = data
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (hitResult.type != HitResult.Type.MISS) {
            if (!isClientWorld(level())) {
                val hitEntity: MCEntity? = (hitResult as? EntityHitResult)?.entity
                data.onImpact(level(), this, getOwner(), hitEntity)
            }
            remove(RemovalReason.DISCARDED)
        }
    }

    override fun readAdditionalSaveData(tag: ValueInput) {
        super.readAdditionalSaveData(tag)
        try {
            data = ThrownLuckyPotionData(
                customDrops = dropsFromStrList(tag.child("impact").get().list("drops", Codec.STRING).get().toList()),
                luck = tag.getIntOr("luck", 0),
                sourceId = tag.getStringOr("sourceId", JavaLuckyRegistry.potionId),
            )
        } catch (e: java.lang.Exception) {
            GAME_API.logError("Failed to read LuckyPotion", e)
            data = ThrownLuckyPotionData()
        }
    }

    override fun addAdditionalSaveData(tag: ValueOutput) {
        super.addAdditionalSaveData(tag)
        val parentTag = CompoundTag()
        data.writeToTag(parentTag)
        tag.store(parentTag)
    }

    override fun getDefaultGravity(): Double {
        return 0.05
    }

    override fun getDefaultItem(): MCItem {
        return ForgeLuckyRegistry.luckyPotion.get()
    }
}

@OnlyInClient
class ThrownLuckyPotionRenderer(ctx: EntityRendererProvider.Context) :
    ThrownItemRenderer<ThrownLuckyPotion>(ctx)
