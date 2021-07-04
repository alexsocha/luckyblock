package mod.lucky.forge.game

import mod.lucky.forge.MCItemStack
import mod.lucky.forge.MCText
import mod.lucky.forge.OnlyInClient
import mod.lucky.forge.isClientWorld
import mod.lucky.java.game.doSwordDrop
import mod.lucky.java.javaGameAPI
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.LivingEntity
import net.minecraft.item.*
import net.minecraft.world.World

class LuckySword : SwordItem(ItemTier.IRON, 3, 2.4f, Properties().defaultDurability(3124).tab(ItemGroup.TAB_COMBAT)) {
    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
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

    override fun getMaxDamage(stack: ItemStack?): Int {
        return 7200
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
