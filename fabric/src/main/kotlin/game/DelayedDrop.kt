package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.game.*
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

class DelayedDrop(
    type: EntityType<DelayedDrop> = FabricLuckyRegistry.delayedDrop,
    world: MCWorld,
    private var data: DelayedDropData = DelayedDropData.createDefault(world),
) : Entity(type, world) {
    override fun defineSynchedData() {}

    override fun tick() {
        super.tick()
        data.tick(level())
        if (data.ticksRemaining <= 0) remove(RemovalReason.DISCARDED)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        data = DelayedDropData.readFromTag(tag, level())
    }
    override fun addAdditionalSaveData(tag: CompoundTag) {
        data.writeToTag(tag)
    }

    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> {
        return ClientboundAddEntityPacket(this)
    }
}

@OnlyInClient
class DelayedDropRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<DelayedDrop>(ctx) {
    override fun getTextureLocation(entity: DelayedDrop): MCIdentifier? {
        return null
    }
}
