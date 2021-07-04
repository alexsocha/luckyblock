package mod.lucky.forge.game

import mod.lucky.forge.CompoundTag
import mod.lucky.forge.ForgeLuckyRegistry
import mod.lucky.forge.MCIdentifier
import mod.lucky.forge.OnlyInClient
import mod.lucky.java.game.*
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.server.SSpawnObjectPacket
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks

class DelayedDrop(
    type: EntityType<DelayedDrop> = ForgeLuckyRegistry.delayedDrop,
    world: World,
    private var data: DelayedDropData = DelayedDropData.createDefault(world),
) : Entity(type, world) {

    override fun defineSynchedData() {}

    override fun tick() {
        super.tick()
        data.tick(level)
        if (data.ticksRemaining <= 0) remove()
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        data = DelayedDropData.readFromTag(tag, level)
    }
    override fun addAdditionalSaveData(tag: CompoundTag) {
        data.writeToTag(tag)
    }

    override fun getAddEntityPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class DelayedDropRenderer(ctx: EntityRendererManager) : EntityRenderer<DelayedDrop>(ctx) {
    override fun getTextureLocation(entity: DelayedDrop): MCIdentifier? {
        return null
    }
}
