package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.game.getLuckModifierCraftingResult
import mod.lucky.java.game.matchesLuckModifierCraftingRecipe
import net.minecraft.inventory.CraftingInventory
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.world.World

class LuckModifierCraftingRecipe(id: MCIdentifier) : SpecialCraftingRecipe(id) {
    override fun matches(inv: CraftingInventory, world: World): Boolean {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getStack(it)) }
        return matchesLuckModifierCraftingRecipe(stacks)
    }

    override fun craft(inv: CraftingInventory): MCItemStack {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getStack(it)) }
        val result = getLuckModifierCraftingResult(stacks)
        return result?.let { toMCItemStack(it) } ?: MCItemStack.EMPTY
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return FabricLuckyRegistry.luckModifierCraftingRecipe
    }
}