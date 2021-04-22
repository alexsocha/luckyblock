package mod.lucky.fabric.game

import mod.lucky.common.gameAPI
import mod.lucky.fabric.FabricLuckyRegistry
import mod.lucky.fabric.OnlyInClient
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf

data class SpawnPacket(
    var entityNBT: CompoundTag
) { companion object }

fun SpawnPacket.Companion.fromEntity(entity: Entity): SpawnPacket {
    val nbt = CompoundTag()
    entity.saveSelfToTag(nbt)
    return SpawnPacket(nbt)
}

fun SpawnPacket.toPacket(): Packet<*> {
    return ServerPlayNetworking.createS2CPacket(FabricLuckyRegistry.spawnPacketId, encode())
}

@OnlyInClient
fun SpawnPacket.execute(world: ClientWorld) {
    val entity = EntityType.loadEntityWithPassengers(entityNBT, world) { e ->
        world.addEntity(e.entityId, e)
        e
    }

    if (entity == null) {
        gameAPI.logError("Error spawning client entity with NBT '$entityNBT'")
        return
    }
}

fun SpawnPacket.Companion.decode(buf: PacketByteBuf): SpawnPacket? {
    val nbt = buf.readCompoundTag()
    if (nbt == null) {
        gameAPI.logError("Error decoding entity spawn packet")
        return null
    }
    return SpawnPacket(nbt)
}

fun SpawnPacket.encode(): PacketByteBuf {
    val buf = PacketByteBufs.create()
    buf.writeCompoundTag(entityNBT)
    return buf
}