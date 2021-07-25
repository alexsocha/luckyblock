package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.toAttr
import mod.lucky.java.javaGameAPI

@OnlyInClient
fun createLuckyTooltip(stack: MCItemStack): List<MCText> {
    val stackNBT = stack.tag?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()

    val luckComponent = when {
        stackNBT.luck == 0 -> MCLiteralText(stackNBT.luck.toString()).withStyle(MCTextFormatting.GOLD)
        stackNBT.luck < 0 -> MCLiteralText(stackNBT.luck.toString()).withStyle(MCTextFormatting.RED)
        else -> MCLiteralText("+${stackNBT.luck}").withStyle(MCTextFormatting.GREEN)
    }

    val nameTooltip = MCTranslatableText("item.lucky.lucky_block.luck")
        .withStyle(MCTextFormatting.GRAY)
        .append(": ")
        .append(luckComponent)

    if (stackNBT.customDrops != null) {
        val dropsTooltip = MCTranslatableText("item.lucky.lucky_block.customDrop")
            .withStyle(MCTextFormatting.GRAY, MCTextFormatting.ITALIC)
        return listOf(nameTooltip, dropsTooltip)
    }
    return listOf(nameTooltip)
}

fun createLuckySubItems(item: MCItem, luckyName: String, unluckyName: String): List<MCItemStack> {
    val luckyStack = MCItemStack(item, 1)
    luckyStack.tag = javaGameAPI.attrToNBT(LuckyItemStackData(luck=80).toAttr()) as CompoundTag
    luckyStack.setHoverName(MCTranslatableText(luckyName))

    val unluckyStack = MCItemStack(item, 1)
    unluckyStack.tag = javaGameAPI.attrToNBT(LuckyItemStackData(luck=-80).toAttr()) as CompoundTag
    unluckyStack.setHoverName(MCTranslatableText(unluckyName))

    return listOf(luckyStack, unluckyStack)
}