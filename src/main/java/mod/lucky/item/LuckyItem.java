package mod.lucky.item;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class LuckyItem {
    private Item containerItem;
    private DropProcessor dropProcessor;

    public LuckyItem(Item containerItem) {
        this.containerItem = containerItem;
        this.dropProcessor = new DropProcessor();
    }

    public Item getContainerItem() { return this.containerItem; }
    public DropProcessor getDropProcessor() { return this.dropProcessor; }

    public boolean hasLuckVariantsInGroup() { return false; }
    public TextComponent getVeryLuckyName() { return null; }
    public TextComponent getUnluckyName() { return null; }

    private boolean isOriginalItem() {
        return this.containerItem == Lucky.luckyBlock.asItem()
            || this.containerItem == Lucky.luckySword
            || this.containerItem == Lucky.luckyBow
            || this.containerItem == Lucky.luckyPotion;
    }

    @OnlyIn(Dist.CLIENT)
    public void addLuckyTooltip(
            ItemStack stack,
            @Nullable World worldIn,
            List<ITextComponent> tooltip,
            ITooltipFlag flagIn) {

        int luck = this.getLuck(stack);
        ITextComponent luckComponent =
            luck == 0 ? new StringTextComponent("" + luck)
                .applyTextStyle(TextFormatting.GOLD)
            : luck < 0 ? new StringTextComponent("" + luck)
                .applyTextStyle(TextFormatting.RED)
            : new StringTextComponent("+" + luck)
                .applyTextStyle(TextFormatting.GREEN);

        tooltip.add(new TranslationTextComponent("item.lucky.lucky_block.luck")
            .appendText(": ")
            .appendSibling(luckComponent));

        String[] drops = this.getRawDrops(stack);
        if (drops != null && drops.length != 0)
            tooltip.add(new TranslationTextComponent("item.lucky.lucky_block.customDrop")
                .applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    public void addLuckySubItems(NonNullList<ItemStack> items) {
        ItemStack normalItemStack = new ItemStack(this.getContainerItem(), 1);
        items.add(normalItemStack);

        if (this.hasLuckVariantsInGroup() && this.isOriginalItem()) {
            CompoundNBT luckyTag = new CompoundNBT();
            luckyTag.putInt("Luck", 80);

            CompoundNBT unluckyTag = new CompoundNBT();
            unluckyTag.putInt("Luck", -80);

            ItemStack luckyItemStack = new ItemStack(this.getContainerItem(), 1);
            luckyItemStack.setTag(luckyTag);
            luckyItemStack.setDisplayName(this.getVeryLuckyName());
            items.add(luckyItemStack);

            ItemStack unluckyItemStack = new ItemStack(this.getContainerItem(), 1);
            unluckyItemStack.setTag(unluckyTag);
            unluckyItemStack.setDisplayName(this.getUnluckyName());
            items.add(unluckyItemStack);
        }
    }

    public static int getLuck(ItemStack stack) {
        try {
            return stack.getTag().getInt("Luck");
        } catch (NullPointerException e) { return 0; }
    }

    @Nullable
    public static String[] getRawDrops(ItemStack stack) {
        try {
            return LuckyUtils.strArrayFromTagList(
                stack.getTag().getList("Drops", Constants.NBT.TAG_STRING));
        } catch (Exception e) { return null; }
    }
}
