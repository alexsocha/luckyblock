package mod.lucky.item;

import java.util.List;
import javax.annotation.Nullable;
import mod.lucky.util.LuckyFunction;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLuckyBlock extends ItemBlock {
  public ItemLuckyBlock(Block block) {
    super(block);
    this.setHasSubtypes(false);
  }

  @Override
  public int getItemStackLimit(ItemStack itemStack) {
    int luckLevel = getLuck(itemStack);
    String[] drops = getDrops(itemStack);
    if (luckLevel == 0 && (drops == null || drops.length == 0)) return 64;
    else return 1;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(
      ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    int luck = getLuck(stack);
    String[] drops = getDrops(stack);
    tooltip.add(
        I18n.translateToLocal("item.luckyBlock.luck")
            + ": "
            + (luck == 0
                ? TextFormatting.GOLD
                : (luck < 0 ? TextFormatting.RED : TextFormatting.GREEN + "+"))
            + String.valueOf(luck));
    if (drops != null && drops.length != 0)
      tooltip.add(
          TextFormatting.GRAY
              + ""
              + TextFormatting.ITALIC
              + I18n.translateToLocal("item.luckyBlock.customDrop"));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
    if (!this.isInCreativeTab(tab)) return;
    ItemStack normalItemStack = new ItemStack(this, 1, 0);
    items.add(normalItemStack);

    if (Block.REGISTRY.getNameForObject(this.getBlock()).toString().equals("lucky:luckyBlock")) {
      NBTTagCompound luckyTag = new NBTTagCompound();
      luckyTag.setInteger("Luck", 80);

      NBTTagCompound unluckyTag = new NBTTagCompound();
      unluckyTag.setInteger("Luck", -80);

      ItemStack luckyItemStack = new ItemStack(this, 1, 0);
      luckyItemStack.setTagCompound(luckyTag);
      luckyItemStack.setStackDisplayName("Very Lucky Block");
      items.add(luckyItemStack);

      ItemStack unluckyItemStack = new ItemStack(this, 1, 0);
      unluckyItemStack.setTagCompound(unluckyTag);
      unluckyItemStack.setStackDisplayName("Unlucky Block");
      items.add(unluckyItemStack);
    }
  }

  public static int getLuck(ItemStack itemStack) {
    try {
      return itemStack.getTagCompound().getInteger("Luck");
    } catch (NullPointerException e) {
      return 0;
    }
  }

  public static String[] getDrops(ItemStack itemStack) {
    try {
      return LuckyFunction.getStringArrayFromNBTTagList(
          (NBTTagList) itemStack.getTagCompound().getTag("Drops"));
    } catch (Exception e) {
      return null;
    }
  }
}
