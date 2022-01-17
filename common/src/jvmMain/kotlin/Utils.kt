package mod.lucky.java

import mod.lucky.common.Random

class JavaRandom(
    private val javaRandom: java.util.Random,
) : Random {
    override fun randInt(range: IntRange): Int = javaRandom.nextInt(range.last - range.first) + range.first
    override fun nextDouble(): Double = javaRandom.nextDouble()
}
