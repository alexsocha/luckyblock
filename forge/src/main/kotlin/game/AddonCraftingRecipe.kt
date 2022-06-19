package mod.lucky.forge.game

import mod.lucky.common.GAME_API
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.loader.ShapedCraftingRecipe
import mod.lucky.java.loader.ShapelessCraftingRecipe
import net.minecraft.core.NonNullList
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraftforge.registries.ForgeRegistries

typealias MCCraftingRecipe = net.minecraft.world.item.crafting.CraftingRecipe
typealias MCShapelessCraftingRecipe = net.minecraft.world.item.crafting.ShapelessRecipe
typealias MCShapedCraftingRecipe = net.minecraft.world.item.crafting.ShapedRecipe

fun getIngredient(id: String): Ingredient? {
    val item = ForgeRegistries.ITEMS.getValue(MCIdentifier(id))
    if (item == null) {
        GAME_API.logError("Invalid item in recipe: $id")
        return null
    }
    return Ingredient.of(item)
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
                    NonNullList.of(Ingredient.EMPTY, *recipe.ingredientIds.mapNotNull { getIngredient(it) }.toTypedArray()),
                )

                is ShapedCraftingRecipe -> MCShapedCraftingRecipe(
                    MCIdentifier(blockId),
                    "lucky",
                    recipe.width,
                    recipe.height,
                    NonNullList.of(Ingredient.EMPTY, *recipe.ingredientIds.map {
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

class AddonCraftingRecipe(id: MCIdentifier) : CustomRecipe(id) {
    companion object {
        lateinit var craftingRecipes: List<MCCraftingRecipe>
    }

    override fun matches(inv: CraftingContainer, world: MCWorld): Boolean {
        return craftingRecipes.find { it.matches(inv, world) } != null
    }

    override fun assemble(inv: CraftingContainer): MCItemStack {
        val matchingRecipe = craftingRecipes.find { it.matches(inv, null) }
        if (matchingRecipe != null) return matchingRecipe.assemble(inv)
        return MCItemStack.EMPTY
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getGroup(): String {
        return "lucky"
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return ForgeLuckyRegistry.addonCraftingRecipe.get()
    }
}
