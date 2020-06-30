package mod.lucky.drop.func;

import java.util.UUID;

import mod.lucky.drop.DropSingle;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class DropProcessData {
    private IWorld world;
    private Entity player;
    private Entity hitEntity;
    private float bowPower = 1;
    private UUID playerUUID;
    private UUID hitEntityUUID;
    private Vector3d harvestPos;
    private DropSingle dropSingle;
    private EnumProcessType processType;

    public DropProcessData(IWorld world) {
        this.world = world;
    }

    public DropProcessData(IWorld world, Entity player, Vector3d harvestPos) {
        this(world, player, harvestPos, null);
    }

    public DropProcessData(IWorld world, Entity player, BlockPos harvestPos) {
        this(world, player,
            new Vector3d(harvestPos.getX() + 0.5, harvestPos.getY(), harvestPos.getZ() + 0.5),
            null);
    }

    public DropProcessData(IWorld world, Entity player, Vector3d harvestPos,
        DropSingle dropSingle) {
        this(world, player, harvestPos, dropSingle, EnumProcessType.NORMAL);
    }

    public DropProcessData(IWorld world, Entity player, Vector3d harvestPos,
                           DropSingle dropSingle, EnumProcessType processType) {
        this(world, player, harvestPos, dropSingle, processType, null);
    }

    public DropProcessData(IWorld world, Entity player, Vector3d harvestPos,
                           DropSingle dropSingle, EnumProcessType processType, Entity hitEntity) {
        this.world = world;
        this.player = player;
        this.harvestPos = harvestPos;
        this.dropSingle = dropSingle;
        this.processType = processType;
        this.hitEntity = hitEntity;
    }

    @Nullable
    public World getWorld() {
        if (this.world instanceof World) return (World) this.world;
        return null;
    }

    public IWorld getRawWorld() {
        return this.world;
    }

    public Entity getPlayer() {
        if (this.player == null && this.world instanceof ServerWorld) {
            this.player = ((ServerWorld) this.world).getEntityByUuid(this.playerUUID);
        }
        if (this.player == null && this.world instanceof ServerWorld) {
            this.player = LuckyUtils.getNearestPlayer((ServerWorld) this.world, this.harvestPos);
        }
        return this.player;
    }

    public Entity getHitEntity() {
        if (this.hitEntity == null && this.world instanceof ServerWorld) {
            this.hitEntity = ((ServerWorld) this.world).getEntityByUuid(this.hitEntityUUID);
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

    public Vector3d getHarvestPos() {
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

    public void setHarvestPos(Vector3d harvestPos) {
        this.harvestPos = harvestPos;
    }

    public void readFromNBT(CompoundNBT tagCompound) {
        this.dropSingle = new DropSingle();
        this.dropSingle.readFromNBT(tagCompound.getCompound("drop"));
        this.harvestPos =
            new Vector3d(
                tagCompound.getDouble("harvestPosX"),
                tagCompound.getDouble("harvestPosY"),
                tagCompound.getDouble("harvestPosZ"));
        this.bowPower = tagCompound.getFloat("bowPower");
        if (tagCompound.contains("playerUUID"))
            this.playerUUID = UUID.fromString(tagCompound.getString("playerUUID"));
        if (tagCompound.contains("hitEntityUUID"))
            this.hitEntityUUID = UUID.fromString(tagCompound.getString("hitEntityUUID"));

        ResourceLocation dimension = new ResourceLocation(tagCompound.contains("world") ? tagCompound.getString("world") : "overworld");
        this.world = Minecraft.getInstance().getIntegratedServer()
            .getWorld(RegistryKey.func_240903_a_(Registry.field_239699_ae_, dimension));
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT mainTag = new CompoundNBT();
        mainTag.put("drop", this.dropSingle.writeToNBT());
        mainTag.putDouble("harvestPosX", this.harvestPos.x);
        mainTag.putDouble("harvestPosY", this.harvestPos.y);
        mainTag.putDouble("harvestPosZ", this.harvestPos.z);
        mainTag.putFloat("bowPower", this.bowPower);
        if (this.player != null || this.playerUUID != null)
            mainTag.putString(
                "playerUUID",
                this.player == null ? this.playerUUID.toString() : this.player.getUniqueID().toString());
        if (this.hitEntity != null || this.hitEntityUUID != null)
            mainTag.putString(
                "hitEntityUUID",
                this.hitEntity == null
                    ? this.hitEntityUUID.toString()
                    : this.hitEntity.getUniqueID().toString());

        ResourceLocation dimension = this.world.getWorld().func_234923_W_().func_240901_a_();
        mainTag.putString("world", dimension.toString());
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
