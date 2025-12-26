package mod.lucky.neoforge.game

import mod.lucky.common.DEFAULT_RANDOM
import mod.lucky.neoforge.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.readFromTag
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import java.util.function.Consumer

class LuckyPotion(registryId: MCIdentifier)
    : MCItem(Properties().setId(ResourceKey.create(Registries.ITEM, registryId))) {

    override fun use(world: MCWorld, user: MCPlayerEntity, hand: InteractionHand): InteractionResult {
        val stack = user.getItemInHand(hand)

        world.playSound(
            null as MCPlayerEntity?,
            user.x, user.y, user.z,
            SoundEvents.SPLASH_POTION_THROW,
            SoundSource.PLAYERS,
            0.5f,
            0.4f / (DEFAULT_RANDOM.nextDouble().toFloat() * 0.4f + 0.8f)
        )
        if (!isClientWorld(world)) {
            val stackData = LuckyItemStackData.readFromTag(componentsToNbt(stack.components, world.registryAccess()))
            val potionEntity = ThrownLuckyPotion(
                world = world,
                user = user,
                data = ThrownLuckyPotionData(
                    customDrops = stackData.customDrops,
                    luck = stackData.luck,
                    sourceId = JAVA_GAME_API.getItemId(stack.item) ?: JavaLuckyRegistry.potionId,
                ),
                itemStack = stack
            )
            potionEntity.item = stack
            potionEntity.shootFromRotation(user, user.xRot, user.yRot, -20.0f, 0.5f, 1.0f)
            world.addFreshEntity(potionEntity)
        }

        user.awardStat(Stats.ITEM_USED.get(this))
        if (!user.abilities.instabuild) stack.shrink(1)

        return if (isClientWorld(world)) InteractionResult.SUCCESS else InteractionResult.SUCCESS_SERVER
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack): Boolean {
        return true
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<MCChatComponent>, flag: TooltipFlag) {
        context.level()?.registryAccess()?.let { access ->
            createLuckyTooltip(stack, access).forEach { tooltipAdder.accept(it) }
        }
    }
}
