package mod.lucky.forge.game

import mod.lucky.common.gameAPI
import mod.lucky.forge.CompoundTag
import mod.lucky.forge.ForgeLuckyRegistry
import mod.lucky.forge.OnlyInClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.*
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkInstance
import net.minecraftforge.fml.network.simple.SimpleChannel
import java.nio.channels.NetworkChannel

/*
data class SpawnPacket(
    var entityNBT: CompoundTag
) { companion object }

fun SpawnPacket.Companion.fromEntity(entity: Entity): SpawnPacket {
    val nbt = CompoundTag()
    entity.saveAsPassenger(nbt)
    return SpawnPacket(nbt)
}

fun SpawnPacket.toPacket(): IPacket<*> {
    return NetworkDirection.PLAY_TO_CLIENT.buildPacket<IPacket<*>>(org.apache.commons.lang3.tuple.Pair.of(encode(), 0), ForgeLuckyRegistry.spawnPacketId).getThis()
}

@OnlyInClient
fun SpawnPacket.execute(world: ClientWorld) {
    val entity = EntityType.loadEntityWithPassengers(entityNBT, world) { e ->
        world.addEntity(e.id, e)
        e
    }

    if (entity == null) {
        gameAPI.logError("Error spawning client entity with NBT '$entityNBT'")
        return
    }
}

fun SpawnPacket.Companion.decode(buf: PacketByteBuf): SpawnPacket? {
    val nbt = buf.readNbt()
    if (nbt == null) {
        gameAPI.logError("Error decoding entity spawn packet")
        return null
    }
    return SpawnPacket(nbt)
}

fun SpawnPacket.encode(): PacketBuffer {
    val buf = PacketBuffer.create()
    buf.writeNbt(entityNBT)
    return buf
}

 */