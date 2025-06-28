package mod.lucky.forge.game

import mod.lucky.common.DEFAULT_RANDOM
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.doBowDrop
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments

class LuckyBow : BowItem(Properties()
    .stacksTo(1)
    .defaultDurability(1000)) {

    override fun releaseUsing(
        stack: MCItemStack, world: MCWorld, player: LivingEntity, timeLeft: Int,
    ) {
        if (player is MCPlayerEntity) {
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
                            sourceId = JAVA_GAME_API.getItemId(this),
                        )
                    }

                    world.playSound(null,
                        player.x,
                        player.y,
                        player.z,
                        SoundEvents.ARROW_SHOOT,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f / (DEFAULT_RANDOM.nextDouble().toFloat() * 0.4f + 1.2f) + power * 0.5f
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

    override fun getUseAnimation(stack: MCItemStack): UseAnim {
        return UseAnim.BOW
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: MCWorld?, tooltip: MutableList<MCChatComponent>, context: TooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}

@OnlyInClient
fun registerLuckyBowModels(item: LuckyBow) {
    ItemProperties.register(
        item,
        MCIdentifier("pulling")
    ) { stack, _, entity, _ ->
        if (entity != null && entity.isUsingItem && entity.useItem === stack) 1.0f else 0.0f
    }

    ItemProperties.register(
        item,
        MCIdentifier("pull")
    ) { stack, _, entity, _ ->
        if (entity == null) {
            0.0f
        } else {
            if (entity.useItem != stack) 0.0f else (stack.useDuration - entity.useItemRemainingTicks).toFloat() / 20.0f;
        }
    }
}

