package mod.lucky.drop.func;

import mod.lucky.drop.DropProperties;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DropFuncItem extends DropFunction {
  @Override
  public void process(DropProcessData processData) {
    DropProperties drop = processData.getDropProperties();
    Item item = Item.getByNameOrId(drop.getPropertyString("ID"));

    ItemStack itemStack;
    itemStack = new ItemStack(item, 1, drop.getPropertyInt("damage"));
    itemStack.setTagCompound(drop.getPropertyNBT("NBTTag"));

    Block.spawnAsEntity(processData.getWorld(), drop.getBlockPos(), itemStack);
  }

  @Override
  public String getType() {
    return "item";
  }
}
