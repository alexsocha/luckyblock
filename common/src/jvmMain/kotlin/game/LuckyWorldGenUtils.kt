package mod.lucky.java.game

import mod.lucky.common.*
import mod.lucky.common.Random
import mod.lucky.common.drop.DEBUG
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.runDrop
import mod.lucky.java.JavaLuckyRegistry
import java.util.*

fun generateLuckyFeature(world: World, surfacePos: Vec3i, blockId: String, dimensionKey: String, random: Random): Boolean {
    val drops = JavaLuckyRegistry.worldGenDrops[blockId]?.get(dimensionKey) ?: return false

    val chosenDrops = drops.filter { random.randInt(0..(it.chance?.toInt() ?: 300)) == 0 }
    if (chosenDrops.isNotEmpty()) {
        val drop = chosenDrops[0]
        if (DEBUG) GAME_API.logInfo("Generatring lucky feature at $surfacePos: $drop")

        val context = DropContext(
            world = world,
            pos = Vec3d(surfacePos.x + 0.5, surfacePos.y.toDouble(), surfacePos.z + 0.5),
            sourceId = blockId,
        )
        runDrop(drop, context, showOutput = false)
        return true
    }
    return false
}