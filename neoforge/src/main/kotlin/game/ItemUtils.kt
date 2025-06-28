package mod.lucky.neoforge.game

import mod.lucky.java.JAVA_GAME_API
import mod.lucky.neoforge.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.LuckyItemValues
import mod.lucky.java.game.readFromTag
import mod.lucky.java.game.toAttr
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents

@OnlyInClient
fun createLuckyTooltip(stack: MCItemStack, access: RegistryAccess): List<MCChatComponent> {
    val nbt = componentsToNbt(stack.components, access)
    val stackData = LuckyItemStackData.readFromTag(nbt)

    val luckComponent = when {
        stackData.luck == 0 -> MCChatComponent.literal(stackData.luck.toString()).withStyle(MCChatFormatting.GOLD)
        stackData.luck < 0 -> MCChatComponent.literal(stackData.luck.toString()).withStyle(MCChatFormatting.RED)
        else -> MCChatComponent.literal("+${stackData.luck}").withStyle(MCChatFormatting.GREEN)
    }

    val nameTooltip = MCChatComponent.translatable("item.lucky.lucky_block.luck")
        .withStyle(MCChatFormatting.GRAY)
        .append(": ")
        .append(luckComponent)

    if (stackData.customDrops != null) {
        val dropsTooltip = MCChatComponent.translatable("item.lucky.lucky_block.customDrop")
            .withStyle(MCChatFormatting.GRAY, MCChatFormatting.ITALIC)
        return listOf(nameTooltip, dropsTooltip)
    }
    return listOf(nameTooltip)
}

fun createLuckySubItems(item: MCItem, access: HolderLookup.Provider): List<MCItemStack> {
    val luckyStack = MCItemStack(item, 1)
    val luckyNbt = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=80).toAttr()) as CompoundTag
    luckyStack.applyComponents(nbtToComponents(luckyNbt, access))
    luckyStack.set(DataComponents.CUSTOM_NAME, MCChatComponent.translatable(LuckyItemValues.veryLuckyBlock))

    val unluckyStack = MCItemStack(item, 1)
    val unluckyNbt = JAVA_GAME_API.attrToNBT(LuckyItemStackData(luck=-80).toAttr()) as CompoundTag
    unluckyStack.applyComponents(nbtToComponents(unluckyNbt, access))
    unluckyStack.set(DataComponents.CUSTOM_NAME, MCChatComponent.translatable(LuckyItemValues.veryUnluckyBlock))

    return listOf(luckyStack, unluckyStack)
}
