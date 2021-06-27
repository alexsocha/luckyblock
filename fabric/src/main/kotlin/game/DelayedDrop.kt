package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.game.*
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.world.World

class DelayedDrop(
    type: EntityType<DelayedDrop> = FabricLuckyRegistry.delayedDrop,
    world: World,
    private var data: DelayedDropData = DelayedDropData.createDefault(world),
) : Entity(type, world) {

    override fun initDataTracker() {}

    override fun tick() {
        super.tick()
        data.tick(world)
        if (data.ticksRemaining <= 0) this.remove(RemovalReason.DISCARDED)
    }

    override fun readCustomDataFromNbt(tag: CompoundTag) {
        data = DelayedDropData.readFromTag(tag, world)
    }
    override fun writeCustomDataToNbt(tag: CompoundTag) {
        data.writeToTag(tag)
    }
    override fun createSpawnPacket(): Packet<*> {
        return EntitySpawnS2CPacket(this)
    }
}

@OnlyInClient
class DelayedDropRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<DelayedDrop>(
    ctx) {
    override fun getTexture(entity: DelayedDrop?): Identifier? {
        return null
    }
}
