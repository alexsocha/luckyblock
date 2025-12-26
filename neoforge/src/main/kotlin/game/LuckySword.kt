package mod.lucky.neoforge.game

import mod.lucky.neoforge.*
import mod.lucky.java.game.doSwordDrop
import mod.lucky.java.JAVA_GAME_API
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import java.util.function.Consumer

class LuckySword(registryId: MCIdentifier) : MCItem(
    Properties()
        .setId(ResourceKey.create(Registries.ITEM, registryId))
        .sword(ToolMaterial.IRON, 3.0F, -2.4F)
        .component(DataComponents.MAX_DAMAGE, 3124)) {


    override fun hurtEnemy(stack: MCItemStack, target: LivingEntity, attacker: LivingEntity) {
        if (!isClientWorld(attacker.level())) {
            doSwordDrop(
                world = attacker.level(),
                player = attacker,
                hitEntity = target,
                stackNBT = componentsToNbt(stack.components, attacker.level().registryAccess()),
                sourceId = JAVA_GAME_API.getItemId(this),
            )
        }
        return super.hurtEnemy(stack, target, attacker)
    }

    override fun getMaxDamage(stack: MCItemStack): Int {
        return 7200
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<MCChatComponent>, tooltipFlag: TooltipFlag) {
        context.level()?.let {
            createLuckyTooltip(stack, it.registryAccess()).forEach {
                tooltipAdder.accept(it)
            }
        }
    }
}
