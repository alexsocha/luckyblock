package mod.lucky.fabric.game

import mod.lucky.common.gameAPI
import mod.lucky.fabric.*
import mod.lucky.java.*
import mod.lucky.java.loader.ShapedCraftingRecipe
import mod.lucky.java.loader.ShapelessCraftingRecipe
import net.minecraft.inventory.CraftingInventory
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

typealias MCCraftingRecipe = net.minecraft.recipe.CraftingRecipe
typealias MCShapelessCraftingRecipe = net.minecraft.recipe.ShapelessRecipe
typealias MCShapedCraftingRecipe = net.minecraft.recipe.ShapedRecipe

fun getIngredient(id: String): Ingredient? {
    val item = Registry.ITEM.getOrEmpty(MCIdentifier(id)).orElse(null)
    if (item == null) {
        gameAPI.logError("Invalid item in recipe: $id")
        return null
    }
    return Ingredient.ofItems(item)
}

fun registerAddonCraftingRecipes() {
    val recipes = JavaLuckyRegistry.allAddonResources.flatMap { addonResources ->
        val blockId = addonResources.addon.ids.block

        if (blockId == null) emptyList<MCShapelessCraftingRecipe>()
        else addonResources.blockCraftingRecipes.mapNotNull { recipe ->
            when (recipe) {
                is ShapelessCraftingRecipe -> MCShapelessCraftingRecipe(
                    MCIdentifier(blockId),
                    "lucky",
                    toMCItemStack(recipe.resultStack),
                    DefaultedList.copyOf(Ingredient.EMPTY, *recipe.ingredientIds.mapNotNull { getIngredient(it) }.toTypedArray()),
                )

                is ShapedCraftingRecipe -> MCShapedCraftingRecipe(
                    MCIdentifier(blockId),
                    "lucky",
                    recipe.width,
                    recipe.height,
                    DefaultedList.copyOf(Ingredient.EMPTY, *recipe.ingredientIds.map {
                        if (it == null) Ingredient.EMPTY else getIngredient(it)
                    }.toTypedArray()),
                    toMCItemStack(recipe.resultStack),
                )

                else -> null
            }
        }
    }

    AddonCraftingRecipe.craftingRecipes = recipes
}

class AddonCraftingRecipe(id: MCIdentifier) : SpecialCraftingRecipe(id) {
    companion object {
        lateinit var craftingRecipes: List<MCCraftingRecipe>
    }

    override fun matches(inv: CraftingInventory, world: World): Boolean {
        return craftingRecipes.find { it.matches(inv, world) } != null
    }

    override fun craft(inv: CraftingInventory): MCItemStack {
        val matchingRecipe = craftingRecipes.find { it.matches(inv, null) }
        if (matchingRecipe != null) return matchingRecipe.craft(inv)
        return MCItemStack.EMPTY
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getGroup(): String {
        return "lucky"
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return FabricLuckyRegistry.addonCraftingRecipe
    }
}
