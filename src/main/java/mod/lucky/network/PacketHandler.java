package mod.lucky.network;

import mod.lucky.Lucky;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation("lucky", "main_channel"))
        .clientAcceptedVersions(v -> v.equals("1"))
        .serverAcceptedVersions(v -> v.equals("1"))
        .networkProtocolVersion(() -> "1")
        .simpleChannel();

    public static void register() {
        CHANNEL.registerMessage(0, SpawnPacket.class,
            SpawnPacket::encode, SpawnPacket::decode, SpawnPacket.Handler::handle);
    }

    public static void spawnEntity(Entity entity) {
        SpawnPacket packet = new SpawnPacket(entity);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
