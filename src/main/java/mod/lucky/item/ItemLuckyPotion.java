package mod.lucky.item;

import java.util.List;
import javax.annotation.Nullable;
import mod.lucky.crafting.LuckyCrafting;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.entity.EntityLuckyPotion;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLuckyPotion extends Item {
  private DropProcessor dropProcessor;
  private LuckyCrafting crafting;

  public ItemLuckyPotion() {
    this.dropProcessor = new DropProcessor();
    this.crafting = new LuckyCrafting(this);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack itemStack = player.getHeldItem(hand);
    if (!player.capabilities.isCreativeMode) {
      itemStack.setCount(itemStack.getCount() - 1);
    }

    world.playSound(
        (EntityPlayer) null,
        player.posX,
        player.posY,
        player.posZ,
        SoundEvents.ENTITY_SPLASH_POTION_THROW,
        SoundCategory.NEUTRAL,
        0.5F,
        0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

    if (!world.isRemote) {
      int luck = ItemLuckyBlock.getLuck(itemStack);
      String[] drops = ItemLuckyBlock.getDrops(itemStack);
      EntityLuckyPotion luckyPotion =
          new EntityLuckyPotion(world, player, this, this.dropProcessor, luck, drops);
      luckyPotion.shoot(
          player, player.rotationPitch, player.rotationYaw, -20.0F, 0.5F, 1.0F);
      world.spawnEntity(luckyPotion);
    }

    player.addStat(StatList.getObjectUseStats(this));
    return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
  }

  public DropProcessor getDropProcessor() {
    return this.dropProcessor;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean hasEffect(ItemStack stack) {
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(
      ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    int luck = ItemLuckyBlock.getLuck(stack);
    String[] drops = ItemLuckyBlock.getDrops(stack);
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

    if (Item.REGISTRY.getNameForObject(this).toString().equals("lucky:luckyPotion")) {
      NBTTagCompound luckyTag = new NBTTagCompound();
      luckyTag.setInteger("Luck", 100);

      NBTTagCompound unluckyTag = new NBTTagCompound();
      unluckyTag.setInteger("Luck", -100);

      ItemStack luckyItemStack = new ItemStack(this, 1, 0);
      luckyItemStack.setTagCompound(luckyTag);
      luckyItemStack.setStackDisplayName("Good Lucky Potion");
      items.add(luckyItemStack);

      ItemStack unluckyItemStack = new ItemStack(this, 1, 0);
      unluckyItemStack.setTagCompound(unluckyTag);
      unluckyItemStack.setStackDisplayName("Bad Lucky Potion");
      items.add(unluckyItemStack);
    }
  }

  public LuckyCrafting getCrafting() {
    return this.crafting;
  }
}
