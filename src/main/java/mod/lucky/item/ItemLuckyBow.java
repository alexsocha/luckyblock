package mod.lucky.item;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLuckyBow extends BowItem implements ILuckyItemContainer {
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
                public float call(ItemStack stack, World worldIn, LivingEntity entityIn) {
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
                public float call(ItemStack stack, World worldIn, LivingEntity entityIn) {
                    return entityIn != null
                        && entityIn.isHandActive()
                        && entityIn.getActiveItemStack() == stack
                        ? 1.0F : 0.0F;
                }
            });
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

    @Override
    public void onPlayerStoppedUsing(
        ItemStack stack, World world, LivingEntity entity, int timeLeft) {

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            ItemStack arrowStack = player.findAmmo(stack);

            boolean unlimitedArrows = player.isCreative()
                || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0
                || (arrowStack.getItem() instanceof ArrowItem
                    && ((ArrowItem) arrowStack.getItem())
                        .isInfinite(arrowStack, stack, player));

            int initPower = this.getUseDuration(stack) - timeLeft;
            initPower =
                ForgeEventFactory.onArrowLoose(
                    stack, world, player, initPower,
                        !arrowStack.isEmpty() || unlimitedArrows);
            if (initPower < 0) return;

            if (unlimitedArrows || !arrowStack.isEmpty()) {
                float power = getArrowVelocity(initPower);
                if (!(power >= 0.1D)) return;

                if (!world.isRemote) {
                    try {
                        int luck = LuckyItem.getLuck(stack);
                        String[] drops = LuckyItem.getRawDrops(stack);

                        ArrowItem arrowitem = (ArrowItem)(
                            stack.getItem() instanceof ArrowItem ? stack.getItem()
                            : Items.ARROW);
                        AbstractArrowEntity arrowEntity = arrowitem.createArrow(
                            world, stack, player);

                        DropProcessData dropData =
                            new DropProcessData(world, player, arrowEntity.getPositionVector())
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

                Vec3d playerPos = player.getPositionVector();
                world.playSound(null,
                    playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_ARROW_SHOOT,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F / (random.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

                if (!unlimitedArrows && !player.abilities.isCreativeMode) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.inventory.deleteStack(arrowStack);
                    }
                }
            }
        }
    }

    @Override
    public int getItemEnchantability() { return 0; }
    @Override
    public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) { return true; }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
        List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.luckyItem.addLuckyTooltip(stack, worldIn, tooltip, flagIn);
    }
}
