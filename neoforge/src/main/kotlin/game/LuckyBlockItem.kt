package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.LuckyItemValues
import net.minecraft.core.NonNullList
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.TooltipFlag

class LuckyBlockItem(block: MCBlock) : BlockItem(
    block,
    Properties()
) {
    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: MCWorld?, tooltip: MutableList<MCChatComponent>, context: TooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}
