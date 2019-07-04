package mod.lucky.item;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLuckyBow extends ItemBow implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this);

    public ItemLuckyBow() {
        super(new Item.Properties()
            .maxStackSize(1)
            .defaultMaxDamage(1000)
            .group(ItemGroup.COMBAT));

        this.addPropertyOverride(
            new ResourceLocation("pull"),
            new IItemPropertyGetter() {
                @Override
                @OnlyIn(Dist.CLIENT)
                public float call(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
                    if (entityIn == null) {
                        return 0.0F;
                    } else {
                        ItemStack itemstack = entityIn.getActiveItemStack();
                        return itemstack != null && itemstack.getItem() instanceof ItemLuckyBow
                            ? (stack.getUseDuration() - entityIn.getItemInUseCount()) / 20.0F
                            : 0.0F;
                    }
                }
            });
        this.addPropertyOverride(
            new ResourceLocation("pulling"),
            new IItemPropertyGetter() {
                @Override
                @OnlyIn(Dist.CLIENT)
                public float call(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
                    return entityIn != null
                        && entityIn.isHandActive()
                        && entityIn.getActiveItemStack() == stack
                        ? 1.0F : 0.0F;
                }
            });
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

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

    @Override
    public void onPlayerStoppedUsing(
        ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;

            boolean unlimitedArrows = player.isCreative()
                || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack arrowStack = this.getInventoryArrows(player);

            int initPower = this.getUseDuration(stack) - timeLeft;
            initPower =
                ForgeEventFactory.onArrowLoose(
                    stack, world, player, initPower, arrowStack != null || unlimitedArrows);
            if (initPower < 0) return;

            if (unlimitedArrows || arrowStack != null) {
                float power = getArrowVelocity(initPower);
                if (!(power >= 0.1D)) return;

                if (!world.isRemote) {
                    try {
                        int luck = LuckyItem.getLuck(stack);
                        String[] drops = LuckyItem.getRawDrops(stack);

                        EntityArrow entityArrow = new EntityTippedArrow(world, player);
                        DropProcessData dropData =
                            new DropProcessData(world, player, entityArrow.getPositionVector())
                                .setBowPower(power * 3.0F);
                        if (drops != null && drops.length != 0)
                            this.getLuckyItem().getDropProcessor()
                                .processRandomDrop(
                                    LuckyUtils.dropsFromStrArray(drops), dropData, luck);
                        else
                            this.getLuckyItem().getDropProcessor()
                                .processRandomDrop(dropData, luck);

                    } catch (Exception e) {
                        Lucky.error(e, DropProcessor.errorMessage());
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
                    1.0F / (random.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

                if (!unlimitedArrows) {
                    arrowStack.setCount(arrowStack.getCount() - 1);
                    if (arrowStack.getCount() == 0) player.inventory.deleteStack(arrowStack);
                }
            }
        }
    }

    @Override
    public int getItemEnchantability() { return 0; }
    @Override
    public EnumAction getUseAction(ItemStack stack) { return EnumAction.BOW; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) { return true; }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
        List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.luckyItem.addLuckyTooltip(stack, worldIn, tooltip, flagIn);
    }
}
