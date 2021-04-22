package mod.lucky.fabric.game

import mod.lucky.fabric.FabricLuckyRegistry
import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import mod.lucky.java.game.LuckyItemValues
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class LuckyBlockItem(block: Block) : BlockItem(
    block,
    FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)
) {
    override fun appendStacks(group: ItemGroup, stacks: DefaultedList<MCItemStack>) {
        if (isIn(group)) {
            stacks.add(MCItemStack(this, 1))
            if (this == FabricLuckyRegistry.luckyBlockItem) {
                stacks.addAll(createLuckySubItems(this, LuckyItemValues.veryLuckyBlock, LuckyItemValues.veryUnluckyBlock))
            }
        }
    }

    @OnlyInClient
    override fun appendTooltip(stack: MCItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}
