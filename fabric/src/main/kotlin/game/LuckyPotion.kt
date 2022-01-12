package mod.lucky.fabric.game

import mod.lucky.common.DEFAULT_RANDOM
import mod.lucky.fabric.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.LuckyItemValues
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.readFromTag
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class LuckyPotion : MCItem(FabricItemSettings().group(ItemGroup.COMBAT)) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack>? {
        val stack = user.getStackInHand(hand)

        world.playSound(
            null as PlayerEntity?,
            user.x, user.y, user.z,
            SoundEvents.ENTITY_SPLASH_POTION_THROW,
            SoundCategory.PLAYERS,
            0.5f,
            0.4f / (DEFAULT_RANDOM.nextDouble().toFloat() * 0.4f + 0.8f)
        )

        if (!isClientWorld(world)) {
            val stackData = stack.nbt?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()
            val potionEntity = ThrownLuckyPotion(
                world = world,
                user = user,
                data = ThrownLuckyPotionData(
                    customDrops = stackData.customDrops,
                    luck = stackData.luck,
                    sourceId = JAVA_GAME_API.getItemId(stack.item) ?: JavaLuckyRegistry.potionId,
                )
            )
            potionEntity.setItem(stack)
            potionEntity.setProperties(user, user.pitch, user.yaw, -20.0f, 0.5f, 1.0f)
            world.spawnEntity(potionEntity)
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this))
        if (!user.abilities.creativeMode) stack.decrement(1)

        return TypedActionResult.success(stack, isClientWorld(world))
    }

    @OnlyInClient
    override fun hasGlint(stack: MCItemStack?): Boolean {
        return true
    }

    override fun appendStacks(group: ItemGroup, stacks: DefaultedList<MCItemStack>) {
        if (isIn(group)) {
            stacks.add(MCItemStack(this, 1))
            if (this == FabricLuckyRegistry.luckyPotion) {
                stacks.addAll(createLuckySubItems(this, LuckyItemValues.veryLuckyPotion, LuckyItemValues.veryUnluckyPotion))
            }
        }
    }

    @OnlyInClient
    override fun appendTooltip(stack: MCItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}
