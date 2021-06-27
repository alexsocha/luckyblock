package mod.lucky.fabric.game

import mod.lucky.common.RANDOM
import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import mod.lucky.fabric.isClientWorld
import mod.lucky.java.*
import mod.lucky.java.game.doBowDrop
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import net.minecraft.world.World

class LuckyBow : BowItem(FabricItemSettings()
    .maxCount(1)
    .maxDamage(1000)
    .group(ItemGroup.COMBAT)) {

    override fun onStoppedUsing(
        stack: MCItemStack, world: World, player: LivingEntity?, timeLeft: Int,
    ) {
        if (player is PlayerEntity) {
            val unlimitedArrows = player.abilities.creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0
            var arrowStack = player.getArrowType(stack)
            if (!arrowStack.isEmpty || unlimitedArrows) {
                if (arrowStack.isEmpty) {
                    arrowStack = MCItemStack(Items.ARROW)
                }
                val i: Int = getMaxUseTime(stack) - timeLeft
                val power = getPullProgress(i)
                if (power >= 0.1f) {
                    if (!isClientWorld(world)) {
                        doBowDrop(
                            world = world,
                            player = player,
                            power = power.toDouble(),
                            stackNBT = stack.tag,
                            sourceId = javaGameAPI.getItemId(this),
                        )
                    }

                    world.playSound(null,
                        player.pos.x,
                        player.pos.y,
                        player.pos.z,
                        SoundEvents.ENTITY_ARROW_SHOOT,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f / (RANDOM.nextFloat() * 0.4f + 1.2f) + power * 0.5f
                    )

                    if (!unlimitedArrows && !player.abilities.creativeMode) {
                        arrowStack.decrement(1)
                        if (arrowStack.isEmpty) {
                            player.inventory.removeOne(arrowStack)
                        }
                    }
                }
            }
        }
    }

    override fun getEnchantability(): Int {
        return 0
    }

    override fun getUseAction(stack: MCItemStack): UseAction {
        return UseAction.BOW
    }

    @OnlyInClient
    override fun hasGlint(stack: MCItemStack?): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendTooltip(stack: MCItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}

@OnlyInClient
fun registerLuckyBowModels(item: LuckyBow) {
    FabricModelPredicateProviderRegistry.register(
        item,
        Identifier("pulling")
    ) { stack, _, entity, _ ->
        if (entity != null && entity.isUsingItem && entity.activeItem === stack) 1.0f else 0.0f
    }

    FabricModelPredicateProviderRegistry.register(
        item,
        Identifier("pull")
    ) { _, _, entity, _ ->
        if (entity == null) {
            0.0f
        } else {
            if (entity.activeItem.item is LuckyBow) entity.itemUseTime / 20.0f else 0.0f
        }
    }
}

