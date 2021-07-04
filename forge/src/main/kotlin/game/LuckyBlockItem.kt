package mod.lucky.forge.game

import mod.lucky.forge.ForgeLuckyRegistry
import mod.lucky.forge.MCItemStack
import mod.lucky.forge.MCText
import mod.lucky.forge.OnlyInClient
import mod.lucky.java.game.LuckyItemValues
import net.minecraft.block.Block
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.util.NonNullList
import net.minecraft.world.World

class LuckyBlockItem(block: Block) : BlockItem(
    block,
    Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)
) {
    override fun fillItemCategory(group: ItemGroup, stacks: NonNullList<MCItemStack>) {
        if (allowdedIn(group)) {
            stacks.add(MCItemStack(this, 1))
            if (this == ForgeLuckyRegistry.luckyBlockItem) {
                stacks.addAll(createLuckySubItems(this, LuckyItemValues.veryLuckyBlock, LuckyItemValues.veryUnluckyBlock))
            }
        }
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: World?, tooltip: MutableList<MCText>, context: ITooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}
