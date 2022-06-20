package mod.lucky.fabric.game

import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import mod.lucky.fabric.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.toAttr
import mod.lucky.java.JAVA_GAME_API
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@OnlyInClient
fun createLuckyTooltip(stack: MCItemStack): List<Text> {
    val stackNBT = stack.nbt?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()

    val luckComponent = when {
        stackNBT.luck == 0 -> MCText.literal(stackNBT.luck.toString()).formatted(Formatting.GOLD)
        stackNBT.luck < 0 -> MCText.literal(stackNBT.luck.toString()).formatted(Formatting.RED)
        else -> MCText.literal("+${stackNBT.luck}").formatted(Formatting.GREEN)
    }

    val nameTooltip = MCText.translatable("item.lucky.lucky_block.luck")
        .formatted(Formatting.GRAY)
        .append(": ")
        .append(luckComponent)

    if (stackNBT.customDrops != null) {
        val dropsTooltip = MCText.translatable("item.lucky.lucky_block.customDrop")
            .formatted(Formatting.GRAY, Formatting.ITALIC)
        return listOf(nameTooltip, dropsTooltip)
    }
    return listOf(nameTooltip)
}

fun createLuckySubItems(item: MCItem, luckyName: String, unluckyName: String): List<MCItemStack> {
    val luckyStack = MCItemStack(item, 1)
    luckyStack.nbt = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=80).toAttr()) as CompoundTag
    luckyStack.setCustomName(MCText.translatable(luckyName))

    val unluckyStack = MCItemStack(item, 1)
    unluckyStack.nbt = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=-80).toAttr()) as CompoundTag
    unluckyStack.setCustomName(MCText.translatable(unluckyName))

    return listOf(luckyStack, unluckyStack)
}
