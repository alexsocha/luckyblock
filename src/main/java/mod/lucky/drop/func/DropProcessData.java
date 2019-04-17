package mod.lucky.drop.func;

import java.util.UUID;
import mod.lucky.command.LuckyCommandLogic;
import mod.lucky.drop.DropProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class DropProcessData {
  private World world;
  private Entity player;
  private Entity hitEntity;
  private float bowPower = 1;
  private UUID playerUUID;
  private UUID hitEntityUUID;
  private Vec3d harvestPos;
  private DropProperties dropProperties;
  private EnumProcessType processType;

  public DropProcessData(World world) {
    this.world = world;
  }

  public DropProcessData(World world, Entity player, Vec3d harvestPos) {
    this(world, player, harvestPos, null);
  }

  public DropProcessData(World world, Entity player, BlockPos harvestPos) {
    this(
        world,
        player,
        new Vec3d(harvestPos.getX() + 0.5, harvestPos.getY(), harvestPos.getZ() + 0.5),
        null);
  }

  public DropProcessData(
      World world, Entity player, Vec3d harvestPos, DropProperties dropProperties) {
    this(world, player, harvestPos, dropProperties, EnumProcessType.NORMAL);
  }

  public DropProcessData(
      World world,
      Entity player,
      Vec3d harvestPos,
      DropProperties dropProperties,
      EnumProcessType processType) {
    this(world, player, harvestPos, dropProperties, processType, null);
  }

  public DropProcessData(
      World world,
      Entity player,
      Vec3d harvestPos,
      DropProperties dropProperties,
      EnumProcessType processType,
      Entity hitEntity) {
    this.world = world;
    this.player = player;
    this.harvestPos = harvestPos;
    this.dropProperties = dropProperties;
    this.processType = processType;
    this.hitEntity = hitEntity;
  }

  public World getWorld() {
    return this.world;
  }

  public Entity getPlayer() {
    if (this.player == null) {
      try {
        this.player = this.world.getMinecraftServer().getEntityFromUuid(this.playerUUID);
      } catch (Exception e) {

      }
    }
    if (this.player == null) {
      try {
        LuckyCommandLogic luckyCommandLogic = new LuckyCommandLogic();
        luckyCommandLogic.setWorld(this.world);
        luckyCommandLogic.setPosition(new BlockPos(this.harvestPos));
        this.player =
            CommandBase.getPlayer(this.world.getMinecraftServer(), luckyCommandLogic, "@p");
      } catch (Exception e) {

      }
    }
    return this.player;
  }

  public Entity getHitEntity() {
    if (this.hitEntity == null) {
      this.hitEntity = this.world.getMinecraftServer().getEntityFromUuid(this.hitEntityUUID);
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

  public DropProperties getDropProperties() {
    return this.dropProperties;
  }

  public EnumProcessType getProcessType() {
    return this.processType;
  }

  public void setProcessType(EnumProcessType processType) {
    this.processType = processType;
  }

  public void setDropProperties(DropProperties properties) {
    this.dropProperties = properties;
  }

  public void setPlayer(Entity player) {
    this.player = player;
  }

  public void setHarvestPos(Vec3d harvestPos) {
    this.harvestPos = harvestPos;
  }

  public void readFromNBT(NBTTagCompound tagCompound) {
    this.dropProperties = new DropProperties();
    this.dropProperties.readFromNBT(tagCompound.getCompoundTag("drop"));
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
    this.world = DimensionManager.getWorld(0);
  }

  public NBTTagCompound writeToNBT() {
    NBTTagCompound mainTag = new NBTTagCompound();
    mainTag.setTag("drop", this.dropProperties.writeToNBT());
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
    return new DropProcessData(
            this.world,
            this.player,
            this.harvestPos,
            this.dropProperties,
            this.processType,
            this.hitEntity)
        .setBowPower(this.bowPower);
  }

  public static enum EnumProcessType {
    NORMAL,
    LUCKY_STRUCT;
  }
}
