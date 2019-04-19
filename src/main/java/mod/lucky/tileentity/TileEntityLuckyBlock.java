package mod.lucky.tileentity;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.util.LuckyFunction;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityLuckyBlock extends TileEntity implements ITickable {
  private String[] drops = new String[0];
  private int luck = 0;

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbttag) {
    super.writeToNBT(nbttag);
    nbttag.setTag("Drops", LuckyFunction.getNBTTagListFromStringArray(this.drops));
    nbttag.setInteger("Luck", this.luck);
    return nbttag;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbttag) {
    super.readFromNBT(nbttag);
    this.drops = LuckyFunction.getStringArrayFromNBTTagList((NBTTagList) nbttag.getTag("Drops"));
    this.luck = nbttag.getInteger("Luck");
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound nbttag = new NBTTagCompound();
    this.writeToNBT(nbttag);
    return new SPacketUpdateTileEntity(
        new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), 1, nbttag);
  }

  @Override
  public void update() {
    if (this.world != null && !this.world.isRemote && this.world.getTotalWorldTime() % 20L == 0L) {
      Block luckyBlock =
          this.world
              .getBlockState(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))
              .getBlock();
      if (luckyBlock == Lucky.luckyBlock) {
        if (this.world.isBlockPowered(
            new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {
          ((BlockLuckyBlock) luckyBlock)
              .removeLuckyBlock(
                  this.world,
                  null,
                  new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()),
                  true);
        }
      }
    }
  }

  public void setLuck(int luck) {
    this.luck = luck;
  }

  public int getLuck() {
    return this.luck;
  }

  public void setDrops(String[] drops) {
    this.drops = drops;
  }

  public String[] getDrops() {
    return this.drops;
  }
}
