package mod.lucky.forge.game

import mod.lucky.forge.*
import mod.lucky.java.game.doSwordDrop
import mod.lucky.java.javaGameAPI
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.TooltipFlag

class LuckySword : SwordItem(Tiers.IRON, 3, 2.4f, Properties().defaultDurability(3124).tab(CreativeModeTab.TAB_COMBAT)) {
    override fun hurtEnemy(stack: MCItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        if (!isClientWorld(attacker.level)) {
            doSwordDrop(
                world = attacker.level,
                player = attacker,
                hitEntity = target,
                stackNBT = stack.tag,
                sourceId = javaGameAPI.getItemId(this),
            )
        }
        return super.hurtEnemy(stack, target, attacker)
    }

    override fun getMaxDamage(stack: MCItemStack?): Int {
        return 7200
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack?): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: MCWorld?, tooltip: MutableList<MCText>, context: TooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}
