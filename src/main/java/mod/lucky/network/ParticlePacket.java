package mod.lucky.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// refer to net.minecraft.network.play.server.SPacketParticles
public class ParticlePacket {
    private IParticleData particle;
    private double x;
    private double y;
    private double z;

    private ParticlePacket() {}

    public static void encode(ParticlePacket msg, PacketBuffer buf) {
        buf.writeInt(IRegistry.field_212632_u.getId(msg.particle.getType()));
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        msg.particle.write(buf);
    }

    public static ParticlePacket decode(PacketBuffer buf) {
        ParticlePacket msg = new ParticlePacket();

        ParticleType particleType = IRegistry.field_212632_u.get(buf.readInt());
        msg.x = buf.readDouble();
        msg.y = buf.readDouble();
        msg.z = buf.readDouble();
        msg.particle = particleType.getDeserializer().read(particleType, buf);

        return msg;
    }

    public static void handle(ParticlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().world.spawnParticle(msg.particle,
                msg.x, msg.y, msg.z,
                0, 0, 0);
        });
        ctx.get().setPacketHandled(true);
    }
}
