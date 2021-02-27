package mod.lucky.crafting;

import mod.lucky.init.SetupCommon;
import mod.lucky.item.ILuckyItemContainer;
import mod.lucky.item.ItemLuckyPotion;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class RecipeLuckCrafting extends SpecialRecipe {
    private static HashMap<ILuckyItemContainer, ArrayList<LuckCraftingModifier>>
        LUCK_MODIFIERS = new HashMap<>();

    public RecipeLuckCrafting(ResourceLocation rl) {
        super(rl);
    }

    public static void addLuckModifier(
        ILuckyItemContainer forItem, LuckCraftingModifier luckModifier) {

        if (!LUCK_MODIFIERS.containsKey(forItem))
            LUCK_MODIFIERS.put(forItem, new ArrayList<>());
        LUCK_MODIFIERS.get(forItem).add(luckModifier);
    }

    @Nullable
    private ItemStack findLuckyStack(IInventory table) {
        ItemStack luckyStack = null;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack.getItem() instanceof ILuckyItemContainer) {
                if (luckyStack != null) return null; // only one lucky item allowed
                luckyStack = stack;
            }
        }
        return luckyStack;
    }

    @Override
    public boolean matches(CraftingInventory table, World world) {
        ItemStack luckyStack = findLuckyStack(table);
        if (luckyStack == null) return false;

        ArrayList<LuckCraftingModifier> luckModifiers =
            LUCK_MODIFIERS.get(luckyStack.getItem());
        if (luckModifiers == null) return false;

        boolean foundModifier = false;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack == luckyStack) continue;

            boolean isModifier = luckModifiers.stream()
                .anyMatch(l -> l.getItem() == stack.getItem());
            if (!isModifier) return false;
            else foundModifier = true;
        }

        return foundModifier;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory table) {
        int baseLuckLevel = 0;
        int itemLuckLevelTotal = 0;
        ItemStack luckyStack = findLuckyStack(table);

        // search whole crafting table
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            int modifierLuck = 0;
            for (LuckCraftingModifier modifier : LUCK_MODIFIERS.get(luckyStack.getItem())) {
                // found luck modifier
                if (stack.getItem() == modifier.getItem()) {
                    modifierLuck = modifier.getLuckValue();
                    if (stack.getItem() instanceof ItemLuckyPotion) modifierLuck *= 4;
                }
            }
            itemLuckLevelTotal += modifierLuck;
        }

        int resultLuckLevel = baseLuckLevel + itemLuckLevelTotal;
        if (resultLuckLevel > 100) resultLuckLevel = 100;
        if (resultLuckLevel < -100) resultLuckLevel = -100;

        ItemStack result = luckyStack.copy();
        result.setCount(1);

        if (resultLuckLevel != 0) {
            if (result.getTag() == null)
                result.setTag(new CompoundNBT());
            result.getTag().putInt("Luck", resultLuckLevel);
        } else result.setTag(null);

        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public String getGroup() {
        return "lucky";
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SetupCommon.LUCK_CRAFTING;
    }
}
