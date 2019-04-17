package mod.lucky.item;

import java.util.List;
import javax.annotation.Nullable;
import mod.lucky.crafting.LuckyCrafting;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyFunction;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLuckyBow extends ItemBow {
  private DropProcessor dropProcessor;
  private LuckyCrafting crafting;

  private String bowTextureName = "lucky:lucky_bow";

  public ItemLuckyBow() {
    super();
    this.setMaxDamage(1000);
    this.dropProcessor = new DropProcessor();
    this.crafting = new LuckyCrafting(this);

    this.addPropertyOverride(
        new ResourceLocation("pull"),
        new IItemPropertyGetter() {
          @Override
          @SideOnly(Side.CLIENT)
          public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
            if (entityIn == null) {
              return 0.0F;
            } else {
              ItemStack itemstack = entityIn.getActiveItemStack();
              return itemstack != null && itemstack.getItem() instanceof ItemLuckyBow
                  ? (stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F
                  : 0.0F;
            }
          }
        });
    this.addPropertyOverride(
        new ResourceLocation("pulling"),
        new IItemPropertyGetter() {
          @Override
          @SideOnly(Side.CLIENT)
          public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
            return entityIn != null
                    && entityIn.isHandActive()
                    && entityIn.getActiveItemStack() == stack
                ? 1.0F
                : 0.0F;
          }
        });
  }

  @Override
  public void onPlayerStoppedUsing(
      ItemStack itemStack, World world, EntityLivingBase entity, int timeLeft) {
    if (entity instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) entity;

      boolean unlimitedArrows =
          player.capabilities.isCreativeMode
              || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemStack) > 0;
      ItemStack arrowStack = this.getInventoryArrows(player);

      int initPower = this.getMaxItemUseDuration(itemStack) - timeLeft;
      initPower =
          net.minecraftforge.event.ForgeEventFactory.onArrowLoose(
              itemStack, world, player, initPower, arrowStack != null || unlimitedArrows);
      if (initPower < 0) return;

      if (unlimitedArrows || arrowStack != null) {
        float power = getArrowVelocity(initPower);
        if (!(power >= 0.1D)) return;

        if (!world.isRemote) {
          try {
            int luck = ItemLuckyBlock.getLuck(itemStack);
            String[] drops = ItemLuckyBlock.getDrops(itemStack);

            EntityArrow entityArrow = new EntityTippedArrow(world, player);
            if (drops != null && drops.length != 0)
              this.getDropProcessor()
                  .processRandomDrop(
                      LuckyFunction.getDropsFromStringArray(drops),
                      new DropProcessData(world, player, entityArrow.getPositionVector())
                          .setBowPower(power * 3.0F),
                      luck);
            else
              this.getDropProcessor()
                  .processRandomDrop(
                      new DropProcessData(world, player, entityArrow.getPositionVector())
                          .setBowPower(power * 3.0F),
                      luck);

          } catch (Exception e) {
            System.err.println(
                "The Lucky Bow encountered and error while trying to perform a function. Error report below:");
            e.printStackTrace();
          }
        }

        world.playSound(
            (EntityPlayer) null,
            player.posX,
            player.posY,
            player.posZ,
            SoundEvents.ENTITY_ARROW_SHOOT,
            SoundCategory.NEUTRAL,
            1.0F,
            1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        if (!unlimitedArrows) {
          // setStackSize(getStackSize() - 1)
          arrowStack.setCount(arrowStack.getCount() - 1);
          // getStackSize()
          if (arrowStack.getCount() == 0) player.inventory.deleteStack(arrowStack);
        }
      }
    }
  }

  private ItemStack getInventoryArrows(EntityPlayer player) {
    if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
      return player.getHeldItem(EnumHand.OFF_HAND);
    } else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
      return player.getHeldItem(EnumHand.MAIN_HAND);
    } else {
      for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
        ItemStack itemstack = player.inventory.getStackInSlot(i);

        if (this.isArrow(itemstack)) {
          return itemstack;
        }
      }

      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  // @Override
  public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining) {
    ModelResourceLocation modelResourceLocation =
        new ModelResourceLocation(this.bowTextureName, "inventory");

    int useTicks = stack.getMaxItemUseDuration() - useRemaining;

    if (stack.getItem() == this && player.getActiveItemStack() != null) {
      if (useTicks >= 18) {
        modelResourceLocation =
            new ModelResourceLocation(this.bowTextureName + "_pulling_2", "inventory");
      } else if (useTicks > 13) {
        modelResourceLocation =
            new ModelResourceLocation(this.bowTextureName + "_pulling_1", "inventory");
      } else if (useTicks > 0) {
        modelResourceLocation =
            new ModelResourceLocation(this.bowTextureName + "_pulling_0", "inventory");
      }
    }
    return modelResourceLocation;
  }

  public void setBowTextureName(String bowTextureName) {
    this.bowTextureName = bowTextureName;
  }

  @Override
  public int getItemEnchantability() {
    return 0;
  }

  @Override
  public EnumAction getItemUseAction(ItemStack stack) {
    return EnumAction.BOW;
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

  public LuckyCrafting getCrafting() {
    return this.crafting;
  }
}
