package mod.lucky.java.loader

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.logger
import mod.lucky.java.ItemStack
import mod.lucky.java.javaGameAPI
import java.lang.Exception

interface CraftingRecipe

data class ShapelessCraftingRecipe(
    val resultStack: ItemStack,
    val ingredientIds: List<String>,
): CraftingRecipe

data class ShapedCraftingRecipe(
    val resultStack: ItemStack,
    val ingredientIds: List<String?>,
    val width: Int,
    val height: Int,
): CraftingRecipe

private fun normalizeId(id: String): String {
    return if (':' in id) id else "minecraft:$id"
}

private fun isValidItemId(id: String): Boolean {
    return ':' in id || javaGameAPI.isValidItemId(id)
}

fun readAddonCraftingRecipes(lines: List<String>, resultId: String): List<CraftingRecipe> {
    return splitLines(lines).mapNotNull { recipeLine ->
        try {
            val recipeParts = recipeLine.split(',')
            if (isValidItemId(recipeParts[0])) {
                ShapelessCraftingRecipe(
                    resultStack = ItemStack(resultId),
                    ingredientIds = recipeParts,
                )
            } else {
                // shaped
                val firstItemIndex = recipeParts.indexOfFirst { isValidItemId(it) }
                val ingredientKeys = (firstItemIndex-1 until recipeParts.size step 2).associate {
                    val itemId = normalizeId(recipeParts[it + 1])
                    val ingredientKey = recipeParts[it].first()
                    ingredientKey to itemId
                } + mapOf(' ' to null)

                val ingredientIds = recipeParts.subList(0, firstItemIndex - 1).flatMap { tableLine ->
                    tableLine.map {
                        if (it in ingredientKeys) ingredientKeys[it]
                        else {
                            logger.logError("Unknown ingredient key '$it' in recipe '$recipeLine'")
                            null
                        }
                    }
                }

                val width = recipeParts[0].length
                val height = firstItemIndex - 1
                ShapedCraftingRecipe(
                    resultStack = ItemStack(resultId),
                    ingredientIds = ingredientIds,
                    width = width,
                    height = height,
                )
            }
        } catch (e: Exception) {
            logger.logError("Error reading addon crafting recipe", e)
            null
        }
    }
}

fun readCraftingLuckModifiers(lines: List<String>): Map<String, Int> {
    return splitLines(lines).mapNotNull {
        try {
            val splitLine = it.split("=")
            val itemId = normalizeId(splitLine[0])
            itemId to parseEvalValue(
                splitLine[1],
                AttrType.INT,
                LuckyRegistry.parserContext,
                LuckyRegistry.simpleEvalContext
            ) as Int
        } catch (e: Exception) {
            logger.logError("Invalid crafting luck modifier '$it'", e)
            null
        }
    }.toMap()
}