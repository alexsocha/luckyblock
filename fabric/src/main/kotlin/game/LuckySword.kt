package mod.lucky.fabric.game

import mod.lucky.fabric.MCItemStack
import mod.lucky.fabric.OnlyInClient
import mod.lucky.fabric.isClientWorld
import mod.lucky.java.game.doSwordDrop
import mod.lucky.java.JAVA_GAME_API
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.*
import net.minecraft.text.Text
import net.minecraft.world.World

class LuckySword : SwordItem(ToolMaterials.IRON, 3, 2.4f, FabricItemSettings().maxDamageIfAbsent(3124).group(ItemGroup.COMBAT)) {
    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        if (!isClientWorld(attacker.world)) {
            doSwordDrop(
                world = attacker.world,
                player = attacker,
                hitEntity = target,
                stackNBT = stack.nbt,
                sourceId = JAVA_GAME_API.getItemId(this),
            )
        }
        return super.postHit(stack, target, attacker)
    }

    override fun getMaxUseTime(stack: ItemStack): Int {
        return 7200
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
