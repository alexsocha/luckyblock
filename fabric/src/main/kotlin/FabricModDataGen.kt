package mod.lucky.fabric

import mod.lucky.fabric.game.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import java.util.concurrent.CompletableFuture


class WorldGenerator(output: FabricDataOutput?, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        entries.addAll(registries.lookupOrThrow(Registries.CONFIGURED_FEATURE))
        entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE))
    }

    override fun getName(): String {
        return "lucky"
    }
}

class FabricModDataGen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::WorldGenerator)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        val featureId = MCIdentifier(FabricLuckyRegistry.luckyWorldFeatureId)
        val feature = LuckyWorldFeature(NoneFeatureConfiguration.CODEC)
        val configuredFeature = ConfiguredFeature(feature, NoneFeatureConfiguration());
        val placedFeature = PlacedFeature(Holder.direct(configuredFeature), emptyList())

        Registry.register(BuiltInRegistries.FEATURE, featureId, feature)

        registryBuilder.add(Registries.CONFIGURED_FEATURE) { registry ->
            val configuredKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, featureId)
            registry.register(configuredKey, configuredFeature)
        }
        registryBuilder.add(Registries.PLACED_FEATURE) { registry ->
            val placedKey = ResourceKey.create(Registries.PLACED_FEATURE, featureId)
            registry.register(placedKey, placedFeature)
        }
    }
}
