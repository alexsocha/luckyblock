package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.getLuckModifierCraftingResult
import mod.lucky.java.game.matchesLuckModifierCraftingRecipe
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.item.crafting.SpecialRecipe
import net.minecraft.world.World

class LuckModifierCraftingRecipe(id: MCIdentifier) : SpecialRecipe(id) {
    override fun matches(inv: CraftingInventory, world: World): Boolean {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getItem(it)) }
        return matchesLuckModifierCraftingRecipe(stacks)
    }

    override fun assemble(inv: CraftingInventory): MCItemStack {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getItem(it)) }
        val result = getLuckModifierCraftingResult(stacks)
        return result?.let { toMCItemStack(it) } ?: MCItemStack.EMPTY
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getSerializer(): IRecipeSerializer<*> {
        return ForgeLuckyRegistry.luckModifierCraftingRecipe
    }
}