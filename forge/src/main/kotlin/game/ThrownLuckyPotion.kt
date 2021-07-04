package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.onImpact
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.writeToTag
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.SpriteRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.IRendersAsItem
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileItemEntity
import net.minecraft.item.Item
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

import net.minecraft.network.IPacket
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem::class)
class ThrownLuckyPotion : ProjectileItemEntity, IRendersAsItem {
    private var data: ThrownLuckyPotionData

    constructor(
        type: EntityType<ThrownLuckyPotion> = ForgeLuckyRegistry.thrownLuckyPotion,
        world: World,
        data: ThrownLuckyPotionData = ThrownLuckyPotionData(),
    ) : super(type, world) {
        this.data = data
    }

    constructor(
        world: World,
        user: LivingEntity,
        data: ThrownLuckyPotionData,
    ) : super(ForgeLuckyRegistry.thrownLuckyPotion, user, world) {
        this.data = data
    }

    override fun onHit(hitResult: RayTraceResult) {
        super.onHit(hitResult)
        if (hitResult.type != RayTraceResult.Type.MISS) {
            if (!isClientWorld(level)) {
                val hitEntity: Entity? = (hitResult as? EntityRayTraceResult)?.entity
                data.onImpact(level, this, owner, hitEntity)
            }
            remove()
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        (javaGameAPI.readNBTKey(tag, "itemLuckyPotion") as? CompoundTag?)?.let {
            javaGameAPI.writeNBTKey(tag, "Item", it)
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

    override fun getDefaultItem(): Item {
        return ForgeLuckyRegistry.luckyPotion
    }

    override fun getAddEntityPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class ThrownLuckyPotionRenderer(ctx: EntityRendererManager) :
    SpriteRenderer<ThrownLuckyPotion>(ctx, Minecraft.getInstance().itemRenderer)
