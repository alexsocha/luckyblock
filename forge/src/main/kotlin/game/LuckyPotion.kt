package mod.lucky.forge.game

import mod.lucky.common.RANDOM
import mod.lucky.forge.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyItemStackData
import mod.lucky.java.game.LuckyItemValues
import mod.lucky.java.game.ThrownLuckyPotionData
import mod.lucky.java.game.readFromTag
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.util.*
import net.minecraft.world.World

class LuckyPotion : MCItem(Properties().tab(ItemGroup.TAB_COMBAT)) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
        val stack = user.getItemInHand(hand)

        world.playSound(
            null as PlayerEntity?,
            user.x, user.y, user.z,
            SoundEvents.SPLASH_POTION_THROW,
            SoundCategory.PLAYERS,
            0.5f,
            0.4f / (RANDOM.nextFloat() * 0.4f + 0.8f)
        )

        if (!isClientWorld(world)) {
            val stackData = stack.tag?.let { LuckyItemStackData.readFromTag(it) } ?: LuckyItemStackData()
            val potionEntity = ThrownLuckyPotion(
                world = world,
                user = user,
                data = ThrownLuckyPotionData(
                    customDrops = stackData.customDrops,
                    luck = stackData.luck,
                    sourceId = javaGameAPI.getItemId(stack.item) ?: JavaLuckyRegistry.potionId,
                )
            )
            potionEntity.item = stack
            potionEntity.shootFromRotation(user, user.xRot, user.yRot, -20.0f, 0.5f, 1.0f)
            world.addFreshEntity(potionEntity)
        }

        user.awardStat(Stats.ITEM_USED.get(this))
        if (!user.abilities.instabuild) stack.shrink(1)

        return ActionResult.sidedSuccess(stack, isClientWorld(world))
    }

    @OnlyInClient
    override fun isFoil(stack: MCItemStack?): Boolean {
        return true
    }

    override fun fillItemCategory(group: ItemGroup, stacks: NonNullList<MCItemStack>) {
        if (allowdedIn(group)) {
            stacks.add(MCItemStack(this, 1))
            if (this == ForgeLuckyRegistry.luckyPotion) {
                stacks.addAll(createLuckySubItems(this, LuckyItemValues.veryLuckyPotion, LuckyItemValues.veryUnluckyPotion))
            }
        }
    }

    @OnlyInClient
    override fun appendHoverText(stack: MCItemStack, world: World?, tooltip: MutableList<MCText>, context: ITooltipFlag) {
        tooltip.addAll(createLuckyTooltip(stack))
    }
}