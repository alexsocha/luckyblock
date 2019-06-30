package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class DropFuncItem extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String itemId = drop.getPropertyString("ID");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            Lucky.error(null, "Invalid item ID: '" + itemId + "'");
            return;
        }

        ItemStack itemStack;
        itemStack = new ItemStack(item, 1);
        itemStack.setTag(drop.getPropertyNBT("NBTTag"));

        Block.spawnAsEntity(processData.getWorld(), drop.getBlockPos(), itemStack);
    }

    @Override
    public String getType() {
        return "item";
    }
}
