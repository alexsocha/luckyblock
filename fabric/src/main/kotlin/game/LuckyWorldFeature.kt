package mod.lucky.fabric.game

import com.mojang.serialization.Codec
import mod.lucky.common.Vec3i
import mod.lucky.common.gameAPI
import mod.lucky.fabric.MCIdentifier
import mod.lucky.fabric.toVec3i
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.game.generateLuckyFeature
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.Heightmap
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.WorldView
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import java.util.*

private fun canGenerateAt(world: WorldView, pos: BlockPos): Boolean {
    val curState = world.getBlockState(pos)
    val soilState = world.getBlockState(pos.down())
    return (
        curState.material.isReplaceable
        && !curState.material.isLiquid
        && soilState.isOpaque
        && soilState.block != Blocks.BEDROCK
    )
}

class LuckyWorldFeature(
    codec: Codec<DefaultFeatureConfig>,
    private val blockId: String = JavaLuckyRegistry.blockId,
) : Feature<DefaultFeatureConfig>(codec) {

    override fun generate(
        world: StructureWorldAccess,
        chunkGenerator: ChunkGenerator,
        random: Random,
        pos: BlockPos,
        config: DefaultFeatureConfig?,
    ): Boolean {
        return try {
            val topPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos)
            val surfaceY = (topPos.y downTo 1).firstOrNull {
                canGenerateAt(world, BlockPos(topPos.x, it, topPos.z))
            }

            if (surfaceY != null) {
                generateLuckyFeature(
                    world = world,
                    surfacePos = Vec3i(topPos.x, surfaceY, topPos.z),
                    blockId = blockId,
                    dimensionKey = world.toServerWorld().registryKey.value.toString(),
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
