package mod.lucky.resources;

import mod.lucky.crafting.LuckCraftingModifier;
import mod.lucky.drop.value.ValueParser;
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
        String value = curLine.substring(curLine.indexOf('=') + 1, curLine.length());

        ItemStack itemStack = ValueParser.getItemStack(name, null);
        int luck = ValueParser.getInteger(value);

        LuckCraftingModifier luckModifier =
            new LuckCraftingModifier(itemStack.getItem(), itemStack.getItemDamage(), luck);
        loader.getBlock().getCrafting().addLuckModifier(luckModifier);
        if (loader.getSword() != null)
          loader.getSword().getCrafting().addLuckModifier(luckModifier);
        if (loader.getBow() != null) loader.getBow().getCrafting().addLuckModifier(luckModifier);
        if (loader.getPotion() != null)
          loader.getPotion().getCrafting().addLuckModifier(luckModifier);
      }
    } catch (Exception e) {
      System.err.println("Lucky Block: Error reading 'luck_crafting.txt'");
      e.printStackTrace();
    }
  }

  @Override
  public String getDirectory() {
    return "luck_crafting.txt";
  }
}
