package mod.lucky.neoforge.game

import com.mojang.serialization.MapCodec
import mod.lucky.neoforge.ForgeLuckyRegistry
import mod.lucky.java.JavaLuckyRegistry
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.InSquarePlacement
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.neoforged.neoforge.common.world.BiomeModifier
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo

class LuckyBiomeModifier : BiomeModifier {
    companion object {
        val INSTANCE = LuckyBiomeModifier()
    }

    override fun modify(
        biome: Holder<Biome>,
        phase: BiomeModifier.Phase,
        builder: ModifiableBiomeInfo.BiomeInfo.Builder
    ) {
        if (phase == BiomeModifier.Phase.ADD) {
            val blockIds = listOf(JavaLuckyRegistry.blockId) + JavaLuckyRegistry.addons.mapNotNull { it.ids.block }
            blockIds.forEach {
                val feature = LuckyWorldFeature(NoneFeatureConfiguration.CODEC, it)
                val placedFeature = PlacedFeature(
                    Holder.direct(ConfiguredFeature(feature, NoneFeatureConfiguration())),
                    listOf(
                        InSquarePlacement.spread(), // random position within the chunk
                        BiomeFilter.biome() // just the current biome
                    )
                )
                builder.generationSettings.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Holder.direct(placedFeature))
            }
        }
    }

    override fun codec(): MapCodec<out BiomeModifier> {
        return ForgeLuckyRegistry.luckyBiomeModifierSerializer.get()
    }
}
