package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.toAttr
import mod.lucky.java.JAVA_GAME_API

@OnlyInClient
fun createLuckyTooltip(stack: MCItemStack): List<MCChatComponent> {
    val stackNBT = stack.tag?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()

    val luckComponent = when {
        stackNBT.luck == 0 -> MCChatComponent.literal(stackNBT.luck.toString()).withStyle(MCChatFormatting.GOLD)
        stackNBT.luck < 0 -> MCChatComponent.literal(stackNBT.luck.toString()).withStyle(MCChatFormatting.RED)
        else -> MCChatComponent.literal("+${stackNBT.luck}").withStyle(MCChatFormatting.GREEN)
    }

    val nameTooltip = MCChatComponent.translatable("item.lucky.lucky_block.luck")
        .withStyle(MCChatFormatting.GRAY)
        .append(": ")
        .append(luckComponent)

    if (stackNBT.customDrops != null) {
        val dropsTooltip = MCChatComponent.translatable("item.lucky.lucky_block.customDrop")
            .withStyle(MCChatFormatting.GRAY, MCChatFormatting.ITALIC)
        return listOf(nameTooltip, dropsTooltip)
    }
    return listOf(nameTooltip)
}

fun createLuckySubItems(item: MCItem, luckyName: String, unluckyName: String): List<MCItemStack> {
    val luckyStack = MCItemStack(item, 1)
    luckyStack.tag = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=80).toAttr()) as CompoundTag
    luckyStack.setHoverName(MCChatComponent.translatable(luckyName))

    val unluckyStack = MCItemStack(item, 1)
    unluckyStack.tag = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=-80).toAttr()) as CompoundTag
    unluckyStack.setHoverName(MCChatComponent.translatable(unluckyName))

    return listOf(luckyStack, unluckyStack)
}
