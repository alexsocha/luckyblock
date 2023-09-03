package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.java.game.getLuckModifierCraftingResult
import mod.lucky.java.game.matchesLuckModifierCraftingRecipe
import net.minecraft.core.RegistryAccess
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer

class LuckModifierCraftingRecipe(id: MCIdentifier, category: CraftingBookCategory) : CustomRecipe(id, category) {
    override fun matches(inv: CraftingContainer, world: MCWorld): Boolean {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getItem(it)) }
        return matchesLuckModifierCraftingRecipe(stacks)
    }

    override fun assemble(inv: CraftingContainer, access: RegistryAccess): MCItemStack {
        val stacks = (0 until inv.width * inv.height).map { toItemStack(inv.getItem(it)) }
        val result = getLuckModifierCraftingResult(stacks)
        return result?.let { toMCItemStack(it) } ?: MCItemStack.EMPTY
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return FabricLuckyRegistry.luckModifierCraftingRecipe
    }
}
