package mod.lucky.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLuckyBlock extends BlockItem implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this) {
        @Override public boolean hasLuckVariantsInGroup() { return true; }
        @Override public TextComponent getVeryLuckyName() {
            return new TranslationTextComponent("block.lucky.lucky_block.veryLucky");
        }
        @Override public TextComponent getUnluckyName() {
            return new TranslationTextComponent("block.lucky.lucky_block.unlucky");
        }
    };

    public ItemLuckyBlock(Block block) {
        super(block, new Item.Properties()
            .group(ItemGroup.BUILDING_BLOCKS));
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

    @Override
    public int getItemStackLimit(ItemStack itemStack) {
        int luckLevel = LuckyItem.getLuck(itemStack);
        String[] drops = LuckyItem.getRawDrops(itemStack);
        if (luckLevel == 0 && (drops == null || drops.length == 0)) return 64;
        else return 1;
    }


    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) this.luckyItem.addLuckySubItems(items);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
        List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.luckyItem.addLuckyTooltip(stack, worldIn, tooltip, flagIn);
    }
}
