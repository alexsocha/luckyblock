package mod.lucky.fabric.game

import mod.lucky.fabric.*
import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import net.minecraft.world.item.BlockItem
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
