package mod.lucky.crafting;

import java.util.ArrayList;

import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.item.ItemLuckyPotion;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LuckCrafting extends ForgeRegistryEntry<IRecipe> implements IRecipe {

    private Block luckCraftingBlock;
    private Item luckCraftingItem;
    private ArrayList<LuckCraftingModifier> luckModifiers;
    private ItemStack resultItemStack;

    public LuckCrafting(Block luckCraftingBlock) {
        this.luckCraftingBlock = luckCraftingBlock;
        this.luckModifiers = new ArrayList<LuckCraftingModifier>();
        this.luckModifiers.add(new LuckCraftingModifier(Items.APPLE, 0, 20));
    }

    public LuckCrafting(Item luckCraftingItem) {
        this.luckCraftingItem = luckCraftingItem;
        this.luckModifiers = new ArrayList<LuckCraftingModifier>();
    }

    public void addLuckModifier(LuckCraftingModifier luckModifier) {
        this.luckModifiers.add(luckModifier);
    }

    @Override
    public boolean matches(IInventory craftingTable, World world) {
        int baseLuckLevel = 0;
        int itemLuckLevelTotal = 0;
        boolean foundModifier = false;
        ItemStack originalStack = null;

        // search whole crafting table
        for (int i = 0; i < craftingTable.getSizeInventory(); i++) {
            ItemStack itemStack = craftingTable.getStackInSlot(i);

            if (itemStack == ItemStack.EMPTY) continue;

                // found main item
            else if ((this.luckCraftingItem != null
                && itemStack.getItem() == this.luckCraftingItem)
                || (this.luckCraftingBlock != null
                && itemStack.getItem() == Item.BLOCK_TO_ITEM.get(this.luckCraftingBlock))) {

                if (originalStack != null) return false; // already found
                baseLuckLevel = ItemLuckyBlock.getLuck(itemStack);
                originalStack = itemStack;

            } else {
                boolean isModifier = false;
                int modifierLuck = 0;

                for (int j = 0; j < this.luckModifiers.size(); j++) {
                    // found luck modifier
                    if (itemStack.getItem() == this.luckModifiers.get(j).getItem()) {
                        isModifier = true;
                        modifierLuck = this.luckModifiers.get(j).getLuckValue();
                        if (this.luckCraftingItem instanceof ItemLuckyPotion) modifierLuck *= 4;
                    }
                }

                if (!isModifier) return false;
                foundModifier = true;
                itemLuckLevelTotal += modifierLuck;
            }
        }
        if (originalStack == null || !foundModifier) return false;

        int resultLuckLevel = baseLuckLevel + itemLuckLevelTotal;
        if (resultLuckLevel > 100) resultLuckLevel = 100;
        if (resultLuckLevel < -100) resultLuckLevel = -100;
        if (baseLuckLevel == 100 && resultLuckLevel == 100) return false;
        if (baseLuckLevel == -100 && resultLuckLevel == -100) return false;

        this.resultItemStack = originalStack.copy();
        this.resultItemStack.setCount(1); // setStackSize(1)

        if (resultLuckLevel != 0) {
            NBTTagCompound tag = this.resultItemStack.getTag();
            if (this.resultItemStack.getTag() == null)
                this.resultItemStack.setTag(new NBTTagCompound());
            this.resultItemStack.getTag().setInt("Luck", resultLuckLevel);
        } else this.resultItemStack.setTag(null);

        return true;
    }

    @Override
    public ItemStack getCraftingResult(IInventory table) {
        return this.resultItemStack.copy();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
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

    @Override
    public ResourceLocation getId() {
        return null;
    }

    @Override
    public IRecipeSerializer getSerializer() {
        return null;
    }
}
