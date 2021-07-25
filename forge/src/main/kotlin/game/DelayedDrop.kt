package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.*
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.network.protocol.Packet
import net.minecraft.world.entity.EntityType
import net.minecraftforge.fmllegacy.network.NetworkHooks

class DelayedDrop(
    type: EntityType<DelayedDrop> = ForgeLuckyRegistry.delayedDrop,
    world: MCWorld,
    private var data: DelayedDropData = DelayedDropData.createDefault(world),
) : MCEntity(type, world) {

    override fun defineSynchedData() {}

    override fun tick() {
        super.tick()
        data.tick(level)
        if (data.ticksRemaining <= 0) remove(RemovalReason.DISCARDED)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        data = DelayedDropData.readFromTag(tag, level)
    }
    override fun addAdditionalSaveData(tag: CompoundTag) {
        data.writeToTag(tag)
    }

    override fun getAddEntityPacket(): Packet<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }
}

@OnlyInClient
class DelayedDropRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<DelayedDrop>(ctx) {
    override fun getTextureLocation(entity: DelayedDrop): MCIdentifier? {
        return null
    }
}
