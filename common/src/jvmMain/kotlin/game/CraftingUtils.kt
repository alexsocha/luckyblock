package mod.lucky.java.game

import mod.lucky.java.ItemStack
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JAVA_GAME_API

fun matchesLuckModifierCraftingRecipe(stacks: List<ItemStack>): Boolean {
    val luckyStacks = stacks.filter { it.itemId in JavaLuckyRegistry.allLuckyItemIds }
    if (luckyStacks.size != 1) return false
    val luckyStack = luckyStacks[0]

    val luckModifiers = JavaLuckyRegistry.craftingLuckModifiers[luckyStack.itemId] ?: return false
    if (!stacks.any { it.itemId in luckModifiers }) return false
    return stacks.all { it.count == 0 || it == luckyStack || it.itemId in luckModifiers }
}

fun getLuckModifierCraftingResult(stacks: List<ItemStack>): ItemStack? {
    val luckyStack = stacks.find { it.itemId in JavaLuckyRegistry.allLuckyItemIds } ?: return null
    val luckModifiers = JavaLuckyRegistry.craftingLuckModifiers[luckyStack.itemId] ?: return null
    val stackData = luckyStack.nbt?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()

    val isPotion = luckyStack.itemId in JavaLuckyRegistry.allLuckyItemIdsByType[JavaLuckyRegistry.potionId]!!
    val luckModifier = stacks.sumBy {
        if (it.count == 0 || it == luckyStack) 0
        else (luckModifiers[it.itemId] ?: 0) * (if (isPotion) 4 else 1)
    }

    val newLuckUnbounded = stackData.luck + luckModifier
    val newLuck = if (newLuckUnbounded > 100) 100 else if (newLuckUnbounded < -100) -100 else newLuckUnbounded
    val newNBT = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck = newLuck).toAttr())
    return ItemStack(luckyStack.itemId, luckyStack.count, newNBT)
}
