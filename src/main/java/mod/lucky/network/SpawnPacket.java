package mod.lucky.network;

import mod.lucky.Lucky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnPacket {
    private CompoundNBT entityTag;

    public SpawnPacket() {}

    public SpawnPacket(Entity entity) {
        this.entityTag = entity.serializeNBT();
    }

    public static SpawnPacket decode(PacketBuffer buf) {
        SpawnPacket msg = new SpawnPacket();
        msg.entityTag = buf.readCompoundTag();
        return msg;
    }

    public static void encode(SpawnPacket msg, PacketBuffer buf) {
        buf.writeCompoundTag(msg.entityTag);
    }

    public static class Handler {
        @OnlyIn(Dist.CLIENT)
        private static void spawn(SpawnPacket msg) {
            ClientWorld world = Minecraft.getInstance().world;

            Entity entity = EntityType.loadEntityAndExecute(msg.entityTag, world, e -> {
                world.addEntity(e.getEntityId(), e);
                return e;
            });

            if (entity == null) {
                Lucky.error(null, "Invalid entity spawning on the client side: "
                    + msg.entityTag.toString());
                return;
            }
        }

        public static void handle(SpawnPacket msg, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                spawn(msg);
            });
            context.get().setPacketHandled(true);
        }
    }
}
