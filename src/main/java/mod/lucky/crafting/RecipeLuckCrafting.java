package mod.lucky.crafting;

import java.util.ArrayList;

import mod.lucky.init.SetupCommon;
import mod.lucky.item.ILuckyItemContainer;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.LuckyItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeHidden;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RecipeLuckCrafting extends IRecipeHidden {
    private static ArrayList<LuckCraftingModifier> luckModifiers;

    public RecipeLuckCrafting(ResourceLocation rl) {
        super(rl);
        this.luckModifiers = new ArrayList<>();
    }

    public static void addLuckModifier(LuckCraftingModifier luckModifier) {
        RecipeLuckCrafting.luckModifiers.add(luckModifier);
    }

    @Override
    public boolean matches(IInventory table, World world) {
        boolean foundModifier = false;
        ItemStack originalStack = null;

        // search whole crafting table
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) continue;
            // found main item
            else if (stack.getItem() instanceof ILuckyItemContainer) {
                if (originalStack != null) return false; // already found
                originalStack = stack;
            } else {
                // check for luck modifier
                boolean isModifier = RecipeLuckCrafting.luckModifiers.stream()
                    .anyMatch(l -> l.getItem() == stack.getItem());
                if (!isModifier) return false;
                else foundModifier = true;
            }
        }
        if (originalStack == null || !foundModifier) return false;
        else return true;
    }

    @Override
    public ItemStack getCraftingResult(IInventory table) {
        int baseLuckLevel = 0;
        int itemLuckLevelTotal = 0;
        ItemStack originalStack = null;

        // search whole crafting table
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) continue;
            // found main item
            else if (stack.getItem() instanceof ILuckyItemContainer) {
                baseLuckLevel = LuckyItem.getLuck(stack);
                originalStack = stack;
            } else {
                int modifierLuck = 0;
                for (LuckCraftingModifier modifier : RecipeLuckCrafting.luckModifiers) {
                    // found luck modifier
                    if (stack.getItem() == modifier.getItem()) {
                        modifierLuck = modifier.getLuckValue();
                        if (stack.getItem() instanceof ItemLuckyPotion) modifierLuck *= 4;
                    }
                }
                itemLuckLevelTotal += modifierLuck;
            }
        }

        int resultLuckLevel = baseLuckLevel + itemLuckLevelTotal;
        if (resultLuckLevel > 100) resultLuckLevel = 100;
        if (resultLuckLevel < -100) resultLuckLevel = -100;

        ItemStack result = originalStack.copy();
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
        return SetupCommon.luckCrafting;
    }
}
