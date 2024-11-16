package mod.lucky.forge.game

import mod.lucky.common.Random
import net.minecraft.util.RandomSource

class MinecraftRandom(
private val minecraftRandom: RandomSource,
) : Random {
    override fun randInt(range: IntRange): Int = minecraftRandom.nextInt(range.last - range.first) + range.first
    override fun nextDouble(): Double = minecraftRandom.nextDouble()
}
