package mod.lucky.crafting;

import java.util.ArrayList;

import mod.lucky.Lucky;
import mod.lucky.init.SetupCommon;
import mod.lucky.item.ILuckyItemContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeHidden;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RecipeAddons extends IRecipeHidden {
    private static ArrayList<IRecipe> ADDON_RECIPES = new ArrayList<>();

    public RecipeAddons(ResourceLocation res) {
        super(res);
    }

    public static void addRecipe(IRecipe recipe) {
        ADDON_RECIPES.add(recipe);
    }

    private static IRecipe getMatchingRecipe(IInventory table) {
        for (IRecipe recipe : ADDON_RECIPES) {
            for (Ingredient i : recipe.getIngredients())
            if (recipe.matches(table, null)) return recipe;
        }
        return null;
    }

    @Override
    public boolean matches(IInventory table, World world) {
        return getMatchingRecipe(table) != null;
    }

    @Override
    public ItemStack getCraftingResult(IInventory table) {
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
