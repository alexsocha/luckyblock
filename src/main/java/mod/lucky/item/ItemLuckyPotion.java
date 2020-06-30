package mod.lucky.item;

import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.network.PacketHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLuckyPotion extends Item implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this) {
        @Override public boolean hasLuckVariantsInGroup() { return true; }
        @Override public TextComponent getVeryLuckyName() {
            return new TranslationTextComponent("item.lucky.lucky_potion.veryLucky");
        }
        @Override public TextComponent getUnluckyName() {
            return new TranslationTextComponent("item.lucky.lucky_potion.unlucky");
        }
    };

    public ItemLuckyPotion() {
        super(new Item.Properties()
            .group(ItemGroup.COMBAT));
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getHeldItem(hand);
        if (!player.isCreative()) {
            stack.setCount(stack.getCount() - 1);
        }

        Vector3d playerPos = player.getPositionVec();
        world.playSound(
            (PlayerEntity) null,
            playerPos.x,
            playerPos.y,
            playerPos.z,
            SoundEvents.ENTITY_SPLASH_POTION_THROW,
            SoundCategory.NEUTRAL,
            0.5F,
            0.4F / (random.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            int luck = LuckyItem.getLuck(stack);
            String[] drops = LuckyItem.getRawDrops(stack);
            EntityLuckyPotion luckyPotion = new EntityLuckyPotion(
                world, player, this, this.getLuckyItem().getDropProcessor(), luck, drops);

            luckyPotion.setShooter(player);
            luckyPotion.shoot(
                player.rotationPitch,
                player.rotationYaw,
                -20.0F,
                0.5F,
                1.0F);

            world.addEntity(luckyPotion);
            PacketHandler.spawnEntity(luckyPotion);
        }

        player.addStat(Stats.ITEM_USED.get(this));
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
        List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.luckyItem.addLuckyTooltip(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) this.luckyItem.addLuckySubItems(items);
    }
}
