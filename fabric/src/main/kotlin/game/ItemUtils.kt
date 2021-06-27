package mod.lucky.fabric.game

import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import mod.lucky.fabric.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.toAttr
import mod.lucky.java.javaGameAPI
import net.minecraft.item.Item
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

@OnlyInClient
fun createLuckyTooltip(stack: MCItemStack): List<Text> {
    val stackNBT = stack.tag?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()

    val luckComponent = when {
        stackNBT.luck == 0 -> LiteralText(stackNBT.luck.toString()).formatted(Formatting.GOLD)
        stackNBT.luck < 0 -> LiteralText(stackNBT.luck.toString()).formatted(Formatting.RED)
        else -> LiteralText("+${stackNBT.luck}").formatted(Formatting.GREEN)
    }

    val nameTooltip = TranslatableText("item.lucky.lucky_block.luck")
        .formatted(Formatting.GRAY)
        .append(": ")
        .append(luckComponent)

    if (stackNBT.customDrops != null) {
        val dropsTooltip = TranslatableText("item.lucky.lucky_block.customDrop")
            .formatted(Formatting.GRAY, Formatting.ITALIC)
        return listOf(nameTooltip, dropsTooltip)
    }
    return listOf(nameTooltip)
}

fun createLuckySubItems(item: Item, luckyName: String, unluckyName: String): List<MCItemStack> {
    val luckyStack = MCItemStack(item, 1)
    luckyStack.tag = javaGameAPI.attrToNBT(LuckyItemStackData(luck=80).toAttr()) as CompoundTag
    luckyStack.setCustomName(TranslatableText(luckyName))

    val unluckyStack = MCItemStack(item, 1)
    unluckyStack.tag = javaGameAPI.attrToNBT(LuckyItemStackData(luck=-80).toAttr()) as CompoundTag
    unluckyStack.setCustomName(TranslatableText(unluckyName))

    return listOf(luckyStack, unluckyStack)
}