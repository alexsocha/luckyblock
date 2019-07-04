package mod.lucky.resources;

import java.util.HashMap;

import mod.lucky.Lucky;
import mod.lucky.crafting.RecipeAddons;
import mod.lucky.drop.value.ValueParser;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ResourceRecipes extends BaseResource {

    private static Ingredient getIngredient(String s) {
        if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(s))) return null;

        Item item = ValueParser.getItem(s, null);
        ItemStack itemStack = ValueParser.getItemStack(s, null);
        if (item != null) return Ingredient.fromItems(item);
        else if (itemStack != null) return Ingredient.fromStacks(itemStack);
        return null;
    }

    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                String[] recipePart = curLine.split(",");
                NonNullList<Ingredient> ingredients = NonNullList.create();
                ItemStack result = new ItemStack(loader.getBlock());

                if (getIngredient(recipePart[0]) != null) {
                    // Shapeless
                    for (String part : recipePart) {
                        Ingredient ingredient = getIngredient(part);
                        if (ingredient != null) ingredients.add(ingredient);
                    }

                    ShapelessRecipe recipe = new ShapelessRecipe(
                        loader.getBlock().getRegistryName(), "lucky",
                        result, ingredients);

                    loader.addRecipe(recipe);
                    RecipeAddons.addRecipe(recipe);
                } else {
                    // Shaped
                    HashMap<Character, Ingredient> ingredientKeys = new HashMap<>();
                    ingredientKeys.put(' ', Ingredient.EMPTY);
                    int recipeEnd = 0;
                    for (int i = recipePart.length - 1; i > 0; i -= 2) {
                        Ingredient ingredient = getIngredient(recipePart[i]);
                        if (ingredient == null) {
                            recipeEnd = i;
                            break;
                        }
                        ingredientKeys.put(recipePart[i - 1].toCharArray()[0], ingredient);
                    }
                    for (int i = 0; i <= recipeEnd; i += 1)
                        for (char c : recipePart[i].toCharArray())
                            ingredients.add(ingredientKeys.get(c));

                    int width = recipePart[0].length(), height = recipeEnd + 1;

                    ShapedRecipe recipe = new ShapedRecipe(
                        loader.getBlock().getRegistryName(), "lucky",
                        width, height, ingredients, result);

                    loader.addRecipe(recipe);
                    RecipeAddons.addRecipe(recipe);
                }
            }
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() { return "recipes.txt"; }
    @Override
    public boolean isOptional() { return true; }

    @Override
    public boolean postInit() {
        return true;
    }
}
