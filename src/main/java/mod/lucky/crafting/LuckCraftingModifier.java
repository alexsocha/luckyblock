package mod.lucky.crafting;

import net.minecraft.item.Item;

public class LuckCraftingModifier {
  private Item item;
  private int damage = -1;
  private int luckValue;

  public LuckCraftingModifier(Item item, int damage, int luckValue) {
    this.item = item;
    this.damage = damage;
    this.luckValue = luckValue;
  }

  public Item getItem() {
    return this.item;
  }

  public int getDamage() {
    return this.damage;
  }

  public int getLuckValue() {
    return this.luckValue;
  }
}
