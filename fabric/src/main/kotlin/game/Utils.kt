package mod.lucky.fabric.game

import mod.lucky.common.Random
import net.minecraft.util.math.random.Random as MCRandom

class MinecraftRandom(
private val minecraftRandom: MCRandom,
) : Random {
    override fun randInt(range: IntRange): Int = minecraftRandom.nextInt(range.last - range.first) + range.first
    override fun nextDouble(): Double = minecraftRandom.nextDouble()
}
