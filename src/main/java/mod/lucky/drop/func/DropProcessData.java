package mod.lucky.drop.func;

import java.util.UUID;

import com.mojang.brigadier.StringReader;
import mod.lucky.drop.DropSingle;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

public class DropProcessData {
    private World world;
    private Entity player;
    private Entity hitEntity;
    private float bowPower = 1;
    private UUID playerUUID;
    private UUID hitEntityUUID;
    private Vec3d harvestPos;
    private DropSingle dropSingle;
    private EnumProcessType processType;

    public DropProcessData(World world) {
        this.world = world;
    }

    public DropProcessData(World world, Entity player, Vec3d harvestPos) {
        this(world, player, harvestPos, null);
    }

    public DropProcessData(World world, Entity player, BlockPos harvestPos) {
        this(world, player,
            new Vec3d(harvestPos.getX() + 0.5, harvestPos.getY(), harvestPos.getZ() + 0.5),
            null);
    }

    public DropProcessData(World world, Entity player, Vec3d harvestPos,
        DropSingle dropSingle) {
        this(world, player, harvestPos, dropSingle, EnumProcessType.NORMAL);
    }

    public DropProcessData(World world, Entity player, Vec3d harvestPos,
                           DropSingle dropSingle, EnumProcessType processType) {
        this(world, player, harvestPos, dropSingle, processType, null);
    }

    public DropProcessData(World world, Entity player, Vec3d harvestPos,
                           DropSingle dropSingle, EnumProcessType processType, Entity hitEntity) {
        this.world = world;
        this.player = player;
        this.harvestPos = harvestPos;
        this.dropSingle = dropSingle;
        this.processType = processType;
        this.hitEntity = hitEntity;
    }

    public World getWorld() {
        return this.world;
    }

    public Entity getPlayer() {
        if (this.player == null && this.world instanceof WorldServer) {
            this.player = ((WorldServer) this.world).getEntityFromUuid(this.playerUUID);
        }
        if (this.player == null && this.world instanceof WorldServer) {
            this.player = LuckyUtils.getNearestPlayer((WorldServer) this.world, this.harvestPos);
        }
        return this.player;
    }

    public Entity getHitEntity() {
        if (this.hitEntity == null && this.world instanceof WorldServer) {
            this.hitEntity = ((WorldServer) this.world).getEntityFromUuid(this.hitEntityUUID);
        }
        return this.hitEntity;
    }

    public float getBowPower() {
        return this.bowPower;
    }

    public DropProcessData setHitEntity(Entity hitEntity) {
        this.hitEntity = hitEntity;
        return this;
    }

    public DropProcessData setBowPower(float bowPower) {
        this.bowPower = bowPower;
        return this;
    }

    public Vec3d getHarvestPos() {
        return this.harvestPos;
    }

    public BlockPos getHarvestBlockPos() {
        return new BlockPos(this.harvestPos.x, this.harvestPos.y, this.harvestPos.z);
    }

    public DropSingle getDropSingle() {
        return this.dropSingle;
    }

    public EnumProcessType getProcessType() {
        return this.processType;
    }

    public void setProcessType(EnumProcessType processType) {
        this.processType = processType;
    }

    public void setDrop(DropSingle properties) {
        this.dropSingle = properties;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setHarvestPos(Vec3d harvestPos) {
        this.harvestPos = harvestPos;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.dropSingle = new DropSingle();
        this.dropSingle.readFromNBT(tagCompound.getCompound("drop"));
        this.harvestPos =
            new Vec3d(
                tagCompound.getDouble("harvestPosX"),
                tagCompound.getDouble("harvestPosY"),
                tagCompound.getDouble("harvestPosZ"));
        this.bowPower = tagCompound.getFloat("bowPower");
        if (tagCompound.hasKey("playerUUID"))
            this.playerUUID = UUID.fromString(tagCompound.getString("playerUUID"));
        if (tagCompound.hasKey("hitEntityUUID"))
            this.hitEntityUUID = UUID.fromString(tagCompound.getString("hitEntityUUID"));
        this.world = Minecraft.getInstance().getIntegratedServer().getWorld(DimensionType.OVERWORLD);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound mainTag = new NBTTagCompound();
        mainTag.setTag("drop", this.dropSingle.writeToNBT());
        mainTag.setDouble("harvestPosX", this.harvestPos.x);
        mainTag.setDouble("harvestPosY", this.harvestPos.y);
        mainTag.setDouble("harvestPosZ", this.harvestPos.z);
        mainTag.setFloat("bowPower", this.bowPower);
        if (this.player != null || this.playerUUID != null)
            mainTag.setString(
                "playerUUID",
                this.player == null ? this.playerUUID.toString() : this.player.getUniqueID().toString());
        if (this.hitEntity != null || this.hitEntityUUID != null)
            mainTag.setString(
                "hitEntityUUID",
                this.hitEntity == null
                    ? this.hitEntityUUID.toString()
                    : this.hitEntity.getUniqueID().toString());
        return mainTag;
    }

    public DropProcessData copy() {
        return new DropProcessData(this.world, this.player, this.harvestPos,
            this.dropSingle, this.processType, this.hitEntity)
            .setBowPower(this.bowPower);
    }

    public static enum EnumProcessType {
        NORMAL,
        LUCKY_STRUCT;
    }
}
