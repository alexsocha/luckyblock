package mod.lucky.fabric.game

import mod.lucky.common.GAME_API
import mod.lucky.fabric.*
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

data class SpawnPacket(
    var entityNBT: CompoundTag
) { companion object }

fun SpawnPacket.Companion.fromEntity(entity: Entity): SpawnPacket {
    val nbt = CompoundTag()
    entity.save(nbt)
    return SpawnPacket(nbt)
}

fun SpawnPacket.toPacket(): Packet<ClientGamePacketListener> {
    return ServerPlayNetworking.createS2CPacket(FabricLuckyRegistry.spawnPacketId, encode())
}

@OnlyInClient
fun SpawnPacket.execute(world: ClientLevel) {
    val entity = EntityType.loadEntityRecursive(entityNBT, world) { e ->
        world.putNonPlayerEntity(0, e)
        e
    }

    if (entity === null) {
        GAME_API.logError("Error spawning client entity with NBT '$entityNBT'")
        return
    }
}

fun SpawnPacket.Companion.decode(buf: FriendlyByteBuf): SpawnPacket? {
    val nbt = buf.readNbt()
    if (nbt === null) {
        GAME_API.logError("Error decoding entity spawn packet")
        return null
    }
    return SpawnPacket(nbt)
}

fun SpawnPacket.encode(): FriendlyByteBuf {
    val buf = PacketByteBufs.create()
    buf.writeNbt(entityNBT)
    return buf
}
