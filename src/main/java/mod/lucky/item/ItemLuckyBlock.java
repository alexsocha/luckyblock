package mod.lucky.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemLuckyBlock extends ItemBlock implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this) {
        @Override public boolean hasLuckVariantsInGroup() { return true; }
        @Override public String getVeryLuckyName() { return "Very Lucky Block"; }
        @Override public String getUnluckyName() { return "Unlucky Block"; }
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
}
