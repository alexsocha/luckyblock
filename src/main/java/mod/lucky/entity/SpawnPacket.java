package mod.lucky.entity;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import mod.lucky.Lucky;
import mod.lucky.init.SetupCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnGlobalEntityPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class SpawnPacket implements IPacket<ClientPlayNetHandler> {
    private int entityId;
    private UUID uniqueId;
    private double x;
    private double y;
    private double z;
    private int speedX;
    private int speedY;
    private int speedZ;
    private int pitch;
    private int yaw;
    private int data;
    private EntityType<?> type;

    public SpawnPacket(Entity entity) {
        this.entityId = entity.getEntityId();
        this.uniqueId = entity.getUniqueID();
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
        this.data = 0;
        this.pitch = MathHelper.floor(entity.rotationPitch * 256.0F / 360.0F);
        this.yaw = MathHelper.floor(entity.rotationYaw * 256.0F / 360.0F);
        this.type = entity.getType();

        Vec3d motion = entity.getMotion();
        this.speedX = (int)(MathHelper.clamp(motion.x, -3.9D, 3.9D) * 8000.0D);
        this.speedY = (int)(MathHelper.clamp(motion.y, -3.9D, 3.9D) * 8000.0D);
        this.speedZ = (int)(MathHelper.clamp(motion.z, -3.9D, 3.9D) * 8000.0D);
    }

    public static void decode(PacketBuffer buf, SpawnPacket msg) {
        msg.entityId = buf.readVarInt();
        msg.uniqueId = buf.readUniqueId();
        msg.type = Registry.ENTITY_TYPE.getByValue(buf.readVarInt());
        msg.x = buf.readDouble();
        msg.y = buf.readDouble();
        msg.z = buf.readDouble();
        msg.pitch = buf.readByte();
        msg.yaw = buf.readByte();
        msg.data = buf.readInt();
        msg.speedX = buf.readShort();
        msg.speedY = buf.readShort();
        msg.speedZ = buf.readShort();
    }

    public static void encode(SpawnPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeUniqueId(msg.uniqueId);
        buf.writeVarInt(Registry.ENTITY_TYPE.getId(msg.type));
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeByte(msg.pitch);
        buf.writeByte(msg.yaw);
        buf.writeInt(msg.data);
        buf.writeShort(msg.speedX);
        buf.writeShort(msg.speedY);
        buf.writeShort(msg.speedZ);
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        decode(buf, this);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        encode(this, buf);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void processPacket(ClientPlayNetHandler handler) {
        ClientWorld world = handler.getWorld();
        EntityType entityType = this.type;

        Entity entity = null;
        if (entityType == SetupCommon.ENTITY_LUCKY_POTION)
            entity = new EntityLuckyPotion(world);
        else if (entityType == SetupCommon.ENTITY_LUCKY_PROJECTILE)
            entity = new EntityLuckyProjectile(world);

        if (entity == null) {
            Lucky.error(null, "Invalid entity spawning on the client side: " + entityType);
            return;
        }

        SpawnPacket msg = this;
        int id = msg.entityId;
        entity.setUniqueId(msg.uniqueId);
        entity.setEntityId(id);
        entity.setPosition(msg.x, msg.y, msg.z);
        entity.func_213312_b(msg.x, msg.y, msg.z); // set server position
        entity.setMotion(new Vec3d(
            msg.speedX / 8000.0D, msg.speedY / 8000.0D, msg.speedZ / 8000.0D));
        entity.rotationPitch = (float)(msg.pitch * 360) / 256.0F;
        entity.rotationYaw = (float)(msg.yaw * 360) / 256.0F;
        world.addEntity(id, entity);
    }
}
