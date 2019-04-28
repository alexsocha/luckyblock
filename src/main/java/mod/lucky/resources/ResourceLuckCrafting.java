package mod.lucky.resources;

import mod.lucky.crafting.LuckCraftingModifier;
import mod.lucky.crafting.RecipeLuckCrafting;
import mod.lucky.drop.value.ValueParser;
import mod.lucky.item.ILuckyItemContainer;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;
import net.minecraft.item.ItemStack;

public class ResourceLuckCrafting extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                String name = curLine.substring(0, curLine.indexOf('='));
                String value = curLine.substring(curLine.indexOf('=') + 1);

                ItemStack itemStack = ValueParser.getItemStack(name, null);
                int luck = ValueParser.getInteger(value);

                LuckCraftingModifier luckModifier =
                    new LuckCraftingModifier(itemStack.getItem(), luck);

                for (ILuckyItemContainer item : loader.getAllItems()) {
                    RecipeLuckCrafting.addLuckModifier(item, luckModifier);
                }
            }
        } catch (Exception e) { this.logError(); }
    }

    @Override
    public String getDirectory() {
        return "luck_crafting.txt";
    }
}
