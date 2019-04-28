package mod.lucky.crafting;

import net.minecraft.item.Item;

public class LuckCraftingModifier {
    private Item item;
    private int luckValue;

    public LuckCraftingModifier(Item item, int luckValue) {
        this.item = item;
        this.luckValue = luckValue;
    }

    public Item getItem() {
        return this.item;
    }

    public int getLuckValue() {
        return this.luckValue;
    }
}
