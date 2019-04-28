package mod.lucky.item;

import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public String getVeryLuckyName() { return ""; }
    public String getUnluckyName() { return ""; }

    @OnlyIn(Dist.CLIENT)
    public void addLuckyTooltip(
            ItemStack stack,
            @Nullable World worldIn,
            List<ITextComponent> tooltip,
            ITooltipFlag flagIn) {

        int luck = this.getLuck(stack);
        ITextComponent luckComponent =
            luck == 0 ? new TextComponentString("" + luck)
                .applyTextStyle(TextFormatting.GOLD)
            : luck < 0 ? new TextComponentString("-" + luck)
                .applyTextStyle(TextFormatting.RED)
            : new TextComponentString("+" + luck)
                .applyTextStyle(TextFormatting.GREEN);

        tooltip.add(new TextComponentTranslation("item.luckyBlock.luck")
            .appendText(": ")
            .appendSibling(luckComponent));

        String[] drops = this.getRawDrops(stack);
        if (drops != null && drops.length != 0)
            tooltip.add(new TextComponentTranslation("item.luckyBlock.customDrop")
                .applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    public void addLuckySubItems(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.getContainerItem().getGroup() != group) return;

        ItemStack normalItemStack = new ItemStack(this.getContainerItem(), 1);
        items.add(normalItemStack);

        if (this.hasLuckVariantsInGroup()) {
            NBTTagCompound luckyTag = new NBTTagCompound();
            luckyTag.setInt("Luck", 80);

            NBTTagCompound unluckyTag = new NBTTagCompound();
            unluckyTag.setInt("Luck", -80);

            ItemStack luckyItemStack = new ItemStack(this.getContainerItem(), 1);
            luckyItemStack.setTag(luckyTag);
            luckyItemStack.setDisplayName(new TextComponentString(this.getVeryLuckyName()));
            items.add(luckyItemStack);

            ItemStack unluckyItemStack = new ItemStack(this.getContainerItem(), 1);
            unluckyItemStack.setTag(unluckyTag);
            unluckyItemStack.setDisplayName(new TextComponentString(this.getUnluckyName()));
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
                (NBTTagList) stack.getTag().getTag("Drops"));
        } catch (Exception e) { return null; }
    }
}
