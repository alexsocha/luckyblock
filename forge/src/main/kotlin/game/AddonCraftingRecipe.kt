package mod.lucky.forge.game

import mod.lucky.common.gameAPI
import mod.lucky.forge.ForgeLuckyRegistry
import mod.lucky.forge.MCIdentifier
import mod.lucky.forge.MCItemStack
import mod.lucky.forge.toMCItemStack
import mod.lucky.java.*
import mod.lucky.java.loader.ShapedCraftingRecipe
import mod.lucky.java.loader.ShapelessCraftingRecipe
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.SpecialRecipe
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.registries.ForgeRegistries

typealias MCCraftingRecipe = net.minecraft.item.crafting.ICraftingRecipe
typealias MCShapelessCraftingRecipe = net.minecraft.item.crafting.ShapelessRecipe
typealias MCShapedCraftingRecipe = net.minecraft.item.crafting.ShapedRecipe

fun getIngredient(id: String): Ingredient? {
    val item = ForgeRegistries.ITEMS.getValue(MCIdentifier(id))
    if (item == null) {
        gameAPI.logError("Invalid item in recipe: $id")
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

class AddonCraftingRecipe(id: MCIdentifier) : SpecialRecipe(id) {
    companion object {
        lateinit var craftingRecipes: List<MCCraftingRecipe>
    }

    override fun matches(inv: CraftingInventory, world: World): Boolean {
        return craftingRecipes.find { it.matches(inv, world) } != null
    }

    override fun assemble(inv: CraftingInventory): MCItemStack {
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

    override fun getSerializer(): IRecipeSerializer<*> {
        return ForgeLuckyRegistry.addonCraftingRecipe
    }
}
