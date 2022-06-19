package mod.lucky.forge.game

import com.mojang.serialization.Codec
import mod.lucky.common.Vec3i
import mod.lucky.common.GAME_API
import mod.lucky.forge.MCBlockPos
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JavaRandom
import mod.lucky.java.game.generateLuckyFeature
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

private fun canGenerateAt(world: WorldGenLevel, pos: MCBlockPos): Boolean {
    val curState = world.getBlockState(pos)
    val soilState = world.getBlockState(pos.below())
    return (
        curState.material.isReplaceable
        && !curState.material.isLiquid
        && soilState.canOcclude()
        && soilState.block != Blocks.BEDROCK
    )
}

class LuckyWorldFeature(
    codec: Codec<NoneFeatureConfiguration>,
    private val blockId: String = JavaLuckyRegistry.blockId,
) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(
        ctx: FeaturePlaceContext<NoneFeatureConfiguration>
    ): Boolean {
        val world = ctx.level()
        val random = ctx.random()
        val pos = ctx.origin()
        return try {
            val topPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos)
            val surfaceY = (topPos.y downTo 1).firstOrNull {
                canGenerateAt(world, MCBlockPos(topPos.x, it, topPos.z))
            }

            if (surfaceY != null) {
                generateLuckyFeature(
                    world = world,
                    surfacePos = Vec3i(topPos.x, surfaceY, topPos.z),
                    blockId = blockId,
                    dimensionKey = world.level.dimension().location().toString(),
                    random = MinecraftRandom(random),
                )
            }
            false
        } catch (e: Exception) {
            GAME_API.logError("Error during natural generation", e)
            false
        }
    }
}
