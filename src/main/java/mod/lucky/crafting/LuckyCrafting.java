package mod.lucky.crafting;

import java.util.ArrayList;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.item.ItemLuckyPotion;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class LuckyCrafting extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
    implements IRecipe {
  private Block luckCraftingBlock;
  private Item luckCraftingItem;
  private ArrayList<CraftingLuckModifier> luckModifiers;
  private ItemStack resultItemStack;

  public LuckyCrafting(Block luckCraftingBlock) {
    this.luckCraftingBlock = luckCraftingBlock;
    this.luckModifiers = new ArrayList<CraftingLuckModifier>();
    this.luckModifiers.add(new CraftingLuckModifier(Items.APPLE, 0, 20));
  }

  public LuckyCrafting(Item luckCraftingItem) {
    this.luckCraftingItem = luckCraftingItem;
    this.luckModifiers = new ArrayList<CraftingLuckModifier>();
  }

  @Override
  public boolean matches(InventoryCrafting craftingTable, World world) {
    int baseLuckLevel = 0;
    int itemLuckLevelTotal = 0;
    boolean foundLuckyCraftingItem = false;
    ItemStack originalLuckyBlock = null;

    for (int i = 0; i < craftingTable.getSizeInventory(); i++) {
      ItemStack itemStack = craftingTable.getStackInSlot(i);

      if (itemStack == ItemStack.EMPTY) {
        continue;
      } else if (this.luckCraftingItem != null
          ? (itemStack.getItem() == this.luckCraftingItem)
          : (this.luckCraftingBlock != null
              ? (itemStack.getItem() == Item.getItemFromBlock(this.luckCraftingBlock))
              : false)) {
        if (originalLuckyBlock != null) return false;
        baseLuckLevel = ItemLuckyBlock.getLuck(itemStack);
        originalLuckyBlock = itemStack;
      } else {
        boolean matchesLuckyItems = false;
        int luckyItemLuck = 0;
        for (int j = 0; j < this.luckModifiers.size(); j++) {
          if (itemStack.getItem() == this.luckModifiers.get(j).getItem()
              && (itemStack.getItemDamage() == this.luckModifiers.get(j).getDamage()
                  || this.luckModifiers.get(j).getDamage() == -1)) {
            matchesLuckyItems = true;
            luckyItemLuck = this.luckModifiers.get(j).getLuckValue();
            if (this.luckCraftingItem instanceof ItemLuckyPotion) luckyItemLuck *= 4;
          }
        }

        if (!matchesLuckyItems) return false;
        foundLuckyCraftingItem = true;

        itemLuckLevelTotal += luckyItemLuck;
      }
    }
    if (originalLuckyBlock == null || !foundLuckyCraftingItem) return false;

    int resultLuckLevel = baseLuckLevel + itemLuckLevelTotal;
    if (resultLuckLevel > 100) resultLuckLevel = 100;
    if (resultLuckLevel < -100) resultLuckLevel = -100;
    if (baseLuckLevel == 100 && resultLuckLevel == 100) return false;
    if (baseLuckLevel == -100 && resultLuckLevel == -100) return false;

    this.resultItemStack = originalLuckyBlock.copy();
    // setStackSize(1)
    this.resultItemStack.setCount(1);

    if (resultLuckLevel != 0) {
      NBTTagCompound tag = this.resultItemStack.getTagCompound();
      if (this.resultItemStack.getTagCompound() == null)
        this.resultItemStack.setTagCompound(new NBTTagCompound());
      this.resultItemStack.getTagCompound().setInteger("Luck", resultLuckLevel);
    } else this.resultItemStack.setTagCompound(null);

    return true;
  }

  @Override
  public ItemStack getCraftingResult(InventoryCrafting table) {
    return this.resultItemStack.copy();
  }

  @Override
  public ItemStack getRecipeOutput() {
    return ItemStack.EMPTY;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventoryCrafting) {
    NonNullList<ItemStack> aitemstack =
        NonNullList.<ItemStack>withSize(
            inventoryCrafting.getSizeInventory(), new ItemStack(Items.STICK));

    for (int i = 0; i < aitemstack.size(); ++i) {
      ItemStack itemstack = inventoryCrafting.getStackInSlot(i);
      aitemstack.set(i, ForgeHooks.getContainerItem(itemstack));
    }

    return aitemstack;
  }

  @Override
  public boolean canFit(int width, int height) {
    return true;
  }

  @Override
  public String getGroup() {
    return "lucky";
  }

  @Override
  public boolean isDynamic() {
    return true;
  }

  public void addLuckModifier(CraftingLuckModifier luckModifier) {
    this.luckModifiers.add(luckModifier);
  }
}
