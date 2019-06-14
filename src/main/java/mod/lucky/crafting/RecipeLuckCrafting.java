package mod.lucky.crafting;

import java.util.ArrayList;
import java.util.HashMap;

import mod.lucky.init.SetupCommon;
import mod.lucky.item.ILuckyItemContainer;
import mod.lucky.item.ItemLuckyPotion;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeHidden;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RecipeLuckCrafting extends IRecipeHidden {
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
    public boolean matches(IInventory table, World world) {
        ItemStack luckyStack = findLuckyStack(table);
        if (luckyStack == null) return false;

        boolean foundModifier = false;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            boolean isModifier = LUCK_MODIFIERS.get(luckyStack.getItem()).stream()
                .anyMatch(l -> l.getItem() == stack.getItem());
            if (!isModifier) return false;
            else foundModifier = true;
        }

        if (!foundModifier) return false;
        else return true;
    }

    @Override
    public ItemStack getCraftingResult(IInventory table) {
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
                result.setTag(new NBTTagCompound());
            result.getTag().setInt("Luck", resultLuckLevel);
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
    public IRecipeSerializer getSerializer() {
        return SetupCommon.LUCK_CRAFTING_SERIALIZER;
    }
}
