package mod.lucky.drop.func;

import mod.lucky.drop.DropProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class DropFuncFill extends DropFunction {
  @Override
  public void process(DropProcessData processData) {
    DropProperties drop = processData.getDropProperties();
    IBlockState blockState = drop.getBlockState();
    BlockPos dropPos = drop.getBlockPos();
    // String blockMode = drop.getPropertyString("blockMode");

    int length = drop.getPropertyInt("length");
    int height = drop.getPropertyInt("height");
    int width = drop.getPropertyInt("width");
    if (drop.hasProperty("pos2X")) {
      length = Math.abs(drop.getPropertyInt("pos2X") - dropPos.getX()) + 1;
      if (dropPos.getX() > drop.getPropertyInt("pos2X")) dropPos = dropPos.add(-length + 1, 0, 0);
    }
    if (drop.hasProperty("pos2Y")) {
      height = Math.abs(drop.getPropertyInt("pos2Y") - dropPos.getY()) + 1;
      if (dropPos.getY() > drop.getPropertyInt("pos2Y")) dropPos = dropPos.add(0, -height + 1, 0);
    }
    if (drop.hasProperty("pos2Z")) {
      width = Math.abs(drop.getPropertyInt("pos2Z") - dropPos.getZ()) + 1;
      if (dropPos.getZ() > drop.getPropertyInt("pos2Z")) dropPos = dropPos.add(0, 0, -width + 1);
    }

    for (int x = dropPos.getX(); x < dropPos.getX() + length; x++) {
      for (int y = dropPos.getY(); y < dropPos.getY() + height; y++) {
        for (int z = dropPos.getZ(); z < dropPos.getZ() + width; z++) {
          DropFuncBlock.setBlock(
              processData.getWorld(),
              blockState,
              new BlockPos(x, y, z),
              drop.getPropertyNBT("NBTTag"),
              drop.getPropertyBoolean("blockUpdate"));
        }
      }
    }
  }

  @Override
  public void registerProperties() {
    DropProperties.setDefaultProperty(this.getType(), "length", Integer.class, 1);
    DropProperties.setDefaultProperty(this.getType(), "height", Integer.class, 1);
    DropProperties.setDefaultProperty(this.getType(), "width", Integer.class, 1);
    DropProperties.setDefaultProperty(this.getType(), "size", String.class, "(1,1,1)");
    DropProperties.setDefaultProperty(this.getType(), "pos2X", Integer.class, 0);
    DropProperties.setDefaultProperty(this.getType(), "pos2Y", Integer.class, 0);
    DropProperties.setDefaultProperty(this.getType(), "pos2Z", Integer.class, 0);
    DropProperties.setDefaultProperty(this.getType(), "pos2", String.class, "(0,0,0)");
    DropProperties.setDefaultProperty(this.getType(), "tileEntity", NBTTagCompound.class, null);
    DropProperties.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
    DropProperties.setDefaultProperty(this.getType(), "blockMode", String.class, "replace");
    DropProperties.setReplaceProperty("tileEntity", "NBTTag");
  }

  @Override
  public String getType() {
    return "fill";
  }
}
