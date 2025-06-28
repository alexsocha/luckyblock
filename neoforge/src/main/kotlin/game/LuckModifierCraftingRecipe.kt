package mod.lucky.forge.game

import mod.lucky.neoforge.*
import mod.lucky.java.game.getLuckModifierCraftingResult
import mod.lucky.java.game.matchesLuckModifierCraftingRecipe
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

class LuckModifierCraftingRecipe(category: CraftingBookCategory) : CustomRecipe(category) {
    override fun matches(inv: CraftingInput, world: Level): Boolean {
        val stacks = (0 until inv.width() * inv.height()).map {
            toItemStack(inv.getItem(it), world.registryAccess(), skipComponents=true)
        }
        return matchesLuckModifierCraftingRecipe(stacks)
    }

    override fun assemble(inv: CraftingInput, access: HolderLookup.Provider): MCItemStack {
        val stacks = (0 until inv.width() * inv.height()).map { toItemStack(inv.getItem(it), access) }
        val result = getLuckModifierCraftingResult(stacks)
        return result?.let { toMCItemStack(it, access) } ?: MCItemStack.EMPTY
    }

    override fun getSerializer(): RecipeSerializer<out CustomRecipe> {
        return ForgeLuckyRegistry.luckModifierCraftingRecipe.get()
    }
}
