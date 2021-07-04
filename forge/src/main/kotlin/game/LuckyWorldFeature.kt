package mod.lucky.forge.game

import com.mojang.serialization.Codec
import mod.lucky.common.Vec3i
import mod.lucky.common.gameAPI
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.game.generateLuckyFeature
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ISeedReader
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.Heightmap
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.NoFeatureConfig
import java.util.*

private fun canGenerateAt(world: ISeedReader, pos: BlockPos): Boolean {
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
    codec: Codec<NoFeatureConfig>,
    private val blockId: String = JavaLuckyRegistry.blockId,
) : Feature<NoFeatureConfig>(codec) {

    override fun place(
        world: ISeedReader,
        chunkGenerator: ChunkGenerator,
        random: Random,
        pos: BlockPos,
        config: NoFeatureConfig,
    ): Boolean {
        return try {
            val topPos = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE, pos)
            val surfaceY = (topPos.y downTo 1).firstOrNull {
                canGenerateAt(world, BlockPos(topPos.x, it, topPos.z))
            }

            if (surfaceY != null) {
                generateLuckyFeature(
                    world = world,
                    surfacePos = Vec3i(topPos.x, surfaceY, topPos.z),
                    blockId = blockId,
                    dimensionKey = world.level.dimension().location().toString(),
                    random = random,
                )
            }
            false
        } catch (e: Exception) {
            gameAPI.logError("Error during natural generation", e)
            false
        }
    }
}
