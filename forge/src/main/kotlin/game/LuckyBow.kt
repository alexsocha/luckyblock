package mod.lucky.forge.game

import mod.lucky.common.RANDOM
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.doBowDrop
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World

class LuckyBow : BowItem(Properties()
    .stacksTo(1)
    .defaultDurability(1000)
    .tab(ItemGroup.TAB_COMBAT)) {

    override fun releaseUsing(
        stack: MCItemStack, world: World, player: LivingEntity?, timeLeft: Int,
    ) {
        if (player is PlayerEntity) {
            val unlimitedArrows = player.abilities.instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0
            var arrowStack = player.getProjectile(stack)
            if (!arrowStack.isEmpty || unlimitedArrows) {
                if (arrowStack.isEmpty) {
                    arrowStack = MCItemStack(Items.ARROW)
                }
                val i: Int = getUseDuration(stack) - timeLeft
                val power = getPowerForTime(i)
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
                        player.x,
                        player.y,
                        player.z,
                        SoundEvents.ARROW_SHOOT,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f / (RANDOM.nextFloat() * 0.4f + 1.2f) + power * 0.5f
                    )

                    if (!unlimitedArrows && !player.abilities.instabuild) {
                        arrowStack.shrink(1)
                        if (arrowStack.isEmpty) {
                            player.inventory.removeItem(arrowStack)
                        }
                    }
                }
            }
        }
    }

    override fun getEnchantmentValue(): Int {
        return 0
    }

    override fun getUseAnimation(stack: MCItemStack): UseAction {
        return UseAction.BOW
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack?): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: World?, tooltip: MutableList<MCText>, context: ITooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}

@OnlyInClient
fun registerLuckyBowModels(item: LuckyBow) {
    ItemModelsProperties.register(
        item,
        MCIdentifier("pulling")
    ) { stack, _, entity ->
        if (entity != null && entity.isUsingItem && entity.useItem === stack) 1.0f else 0.0f
    }

    ItemModelsProperties.register(
        item,
        MCIdentifier("pull")
    ) { _, _, entity ->
        if (entity == null) {
            0.0f
        } else {
            if (entity.useItem.item is LuckyBow) entity.useItemRemainingTicks / 20.0f else 0.0f
        }
    }
}

