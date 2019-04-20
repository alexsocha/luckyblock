package mod.lucky.item;

import mod.lucky.entity.EntityLuckyPotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemLuckyPotion extends Item implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this) {
        @Override public boolean hasLuckVariantsInGroup() { return true; }
        @Override public String getVeryLuckyName() { return "Very Lucky Potion"; }
        @Override public String getUnluckyName() { return "Unlucky Potion"; }
    };

    public ItemLuckyPotion() {
        super(new Item.Properties());
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {

        ItemStack stack = player.getHeldItem(hand);
        if (!player.isCreative()) {
            stack.setCount(stack.getCount() - 1);
        }

        world.playSound(
            (EntityPlayer) null,
            player.posX,
            player.posY,
            player.posZ,
            SoundEvents.ENTITY_SPLASH_POTION_THROW,
            SoundCategory.NEUTRAL,
            0.5F,
            0.4F / (random.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            int luck = LuckyItem.getLuck(stack);
            String[] drops = LuckyItem.getRawDrops(stack);
            EntityLuckyPotion luckyPotion = new EntityLuckyPotion(
                world, player, this, this.getLuckyItem().getDropProcessor(), luck, drops);

            luckyPotion.shoot(
                player,
                player.rotationPitch,
                player.rotationYaw,
                -20.0F,
                0.5F,
                1.0F);

            world.spawnEntity(luckyPotion);
        }

        player.addStat(StatList.ITEM_USED.get(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
