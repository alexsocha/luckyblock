package mod.lucky.neoforge.game

import mod.lucky.neoforge.*
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.block.Block
import java.util.function.Consumer

class LuckyBlockItem(block: Block, registryId: MCIdentifier) : BlockItem(
    block,
    Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryId))
) {
    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<MCChatComponent>, tooltipFlag: TooltipFlag) {
        context.level()?.let {
            createLuckyTooltip(stack, it.registryAccess()).forEach {
                tooltipAdder.accept(it)
            }
        }
    }
}
