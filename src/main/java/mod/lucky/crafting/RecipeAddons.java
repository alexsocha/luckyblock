package mod.lucky.crafting;

import mod.lucky.init.SetupCommon;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;

public class RecipeAddons extends SpecialRecipe {
    private static ArrayList<IRecipe> ADDON_RECIPES = new ArrayList<>();

    public RecipeAddons(ResourceLocation res) {
        super(res);
    }

    public static void addRecipe(IRecipe recipe) {
        ADDON_RECIPES.add(recipe);
    }

    private static IRecipe getMatchingRecipe(IInventory table) {
        for (IRecipe recipe : ADDON_RECIPES) {
            if (recipe.matches(table, null)) return recipe;
        }
        return null;
    }

    @Override
    public boolean matches(CraftingInventory table, World world) {
        return getMatchingRecipe(table) != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory table) {
        return getMatchingRecipe(table).getCraftingResult(table);
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
        return SetupCommon.ADDON_CRAFTING;
    }
}
