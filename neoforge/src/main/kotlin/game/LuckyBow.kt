package mod.lucky.neoforge.game

import mod.lucky.common.DEFAULT_RANDOM
import mod.lucky.java.*
import mod.lucky.java.game.doBowDrop
import mod.lucky.neoforge.*
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import java.util.function.Consumer

class LuckyBow(registryId: MCIdentifier) : BowItem(
    Item.Properties()
        .setId(ResourceKey.create(Registries.ITEM, registryId))
        .stacksTo(1)
        .durability(1000)
) {
    override fun releaseUsing(stack: MCItemStack, world: Level, player: LivingEntity, timeLeft: Int): Boolean {
        if (player is MCPlayerEntity) {
            // val unlimitedArrows = player.abilities.instabuild || EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0
            var arrowStack = player.getProjectile(stack)
            if (arrowStack.isEmpty) {
                return false
            } else {
                val i: Int = getUseDuration(stack, player) - timeLeft
                val power = getPowerForTime(i)
                if (i < 0 || power < 0.1f) {
                    return false
                } else {
                    draw(stack, arrowStack, player)

                    if (!isClientWorld(world)) {
                        doBowDrop(
                            world = world,
                            player = player,
                            power = power.toDouble(),
                            stackNBT = componentsToNbt(stack.components, world.registryAccess()),
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

                    /*
                    if (!unlimitedArrows && !player.abilities.instabuild) {
                        arrowStack.shrink(1)
                        if (arrowStack.isEmpty) {
                            player.inventory.removeItem(arrowStack)
                        }
                    }
                    */
                    return true;
                }
            }
        }
        return false;
    }

    override fun getEnchantmentLevel(stack: ItemStack, enchantment: Holder<Enchantment>): Int {
        return 0;
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipDisplay: TooltipDisplay,
        tooltipAdder: Consumer<Component>,
        flag: TooltipFlag
    ) {
        context.level()?.registryAccess()?.let { access ->
            createLuckyTooltip(stack, access).forEach { tooltipAdder.accept(it) }
        }
    }
}
