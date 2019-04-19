package mod.lucky.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ParticlePacket implements IMessage {
  private NBTTagCompound tag;

  public ParticlePacket() {}

  public ParticlePacket(String particleName, double posX, double posY, double posZ) {
    this.tag = new NBTTagCompound();
    this.tag.setString("name", particleName);
    this.tag.setDouble("x", posX);
    this.tag.setDouble("y", posY);
    this.tag.setDouble("z", posZ);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.tag = ByteBufUtils.readTag(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeTag(buf, this.tag);
  }

  public static class Handler implements IMessageHandler<ParticlePacket, IMessage> {
    @Override
    public IMessage onMessage(ParticlePacket message, MessageContext ctx) {
      String particleName = message.tag.getString("name");
      EnumParticleTypes particle = EnumParticleTypes.EXPLOSION_NORMAL;
      for (EnumParticleTypes particleType : EnumParticleTypes.values()) {
        if (particleType.getParticleName().equals(particleName)) {
          particle = particleType;
          break;
        }
      }
      Minecraft.getMinecraft()
          .world
          .spawnParticle(
              particle,
              message.tag.getDouble("x"),
              message.tag.getDouble("y"),
              message.tag.getDouble("z"),
              0,
              0,
              0);
      return null;
    }
  }
}
