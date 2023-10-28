package mod.lucky.fabric

import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.fabric.game.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyItemValues
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.*
import net.fabricmc.loader.impl.metadata.ModOriginImpl
import net.fabricmc.loader.impl.util.FileSystemUtil
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import java.util.*

object FabricLuckyRegistry {
    val LOGGER: Logger = LogManager.getLogger()
    val luckyBlock = LuckyBlock()
    val luckyBlockItem = LuckyBlockItem(luckyBlock)
    val luckyBow = LuckyBow()
    val luckySword = LuckySword()
    val luckyPotion = LuckyPotion()
    val luckyWorldFeatureId = "lucky:lucky_world_gen"
    lateinit var luckyBlockEntity: BlockEntityType<LuckyBlockEntity>
    lateinit var luckyProjectile: EntityType<LuckyProjectile>
    lateinit var thrownLuckyPotion: EntityType<ThrownLuckyPotion>
    lateinit var delayedDrop: EntityType<DelayedDrop>
    lateinit var luckModifierCraftingRecipe: SimpleCraftingRecipeSerializer<LuckModifierCraftingRecipe>
    lateinit var addonCraftingRecipe: SimpleCraftingRecipeSerializer<AddonCraftingRecipe>
}

class FabricMod : ModInitializer {
    init {
        PLATFORM_API = JavaPlatformAPI
        LOGGER = FabricGameAPI
        GAME_API = FabricGameAPI
        JAVA_GAME_API = FabricJavaGameAPI
    }

    private fun registerWorldGen() {
        val featureId = MCIdentifier(FabricLuckyRegistry.luckyWorldFeatureId)
        val feature = LuckyWorldFeature(NoneFeatureConfiguration.CODEC)

        Registry.register(BuiltInRegistries.FEATURE, featureId, feature)

        BiomeModifications.addFeature(
            BiomeSelectors.all(),
            GenerationStep.Decoration.SURFACE_STRUCTURES,
            ResourceKey.create(Registries.PLACED_FEATURE, MCIdentifier(FabricLuckyRegistry.luckyWorldFeatureId))
        );
    }

    fun setupCreativeTabs() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register { group ->
            group.accept(FabricLuckyRegistry.luckyBlockItem)
            createLuckySubItems(FabricLuckyRegistry.luckyBlockItem, LuckyItemValues.veryLuckyBlock, LuckyItemValues.veryUnluckyBlock).forEach { group.accept(it) }
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register { group ->
            group.accept(FabricLuckyRegistry.luckySword)
            group.accept(FabricLuckyRegistry.luckyBow)
            group.accept(FabricLuckyRegistry.luckyPotion)
            createLuckySubItems(FabricLuckyRegistry.luckyPotion, LuckyItemValues.veryLuckyPotion, LuckyItemValues.veryUnluckyPotion).forEach { group.accept(it) }
        }

        for (addon in JavaLuckyRegistry.addons) {
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register { group ->
                if (addon.ids.block != null) group.accept(BuiltInRegistries.ITEM.get(MCIdentifier(addon.ids.block!!)))
            }
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register { group ->
                if (addon.ids.sword != null) group.accept(BuiltInRegistries.ITEM.get(MCIdentifier(addon.ids.sword!!)))
                if (addon.ids.bow != null) group.accept(BuiltInRegistries.ITEM.get(MCIdentifier(addon.ids.bow!!)))
                if (addon.ids.potion != null) group.accept(BuiltInRegistries.ITEM.get(MCIdentifier(addon.ids.potion!!)))
            }
        }
    }

    override fun onInitialize() {
        FabricGameAPI.init()
        JavaLuckyRegistry.init()

        FabricLuckyRegistry.luckyBlockEntity = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            JavaLuckyRegistry.blockId,
            BlockEntityType.Builder.of(::LuckyBlockEntity, FabricLuckyRegistry.luckyBlock).build(null)
        )
        FabricLuckyRegistry.luckyProjectile = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            JavaLuckyRegistry.projectileId,
            FabricEntityTypeBuilder.create(MobCategory.MISC, ::LuckyProjectile)
                .trackRangeChunks(100)
                .trackedUpdateRate(20)
                .forceTrackedVelocityUpdates(true)
                .build()
        )
        FabricLuckyRegistry.thrownLuckyPotion = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            JavaLuckyRegistry.potionId,
            FabricEntityTypeBuilder.create(MobCategory.MISC, ::ThrownLuckyPotion)
                .trackRangeChunks(100)
                .trackedUpdateRate(20)
                .forceTrackedVelocityUpdates(true)
                .build()
        )

        FabricLuckyRegistry.delayedDrop = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            JavaLuckyRegistry.delayedDropId,
            FabricEntityTypeBuilder.create(MobCategory.MISC, ::DelayedDrop).build()
        )

        FabricLuckyRegistry.luckModifierCraftingRecipe = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            MCIdentifier("lucky:crafting_luck"),
            SimpleCraftingRecipeSerializer(::LuckModifierCraftingRecipe),
        )
        FabricLuckyRegistry.addonCraftingRecipe = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            MCIdentifier("lucky:crafting_addons"),
            SimpleCraftingRecipeSerializer(::AddonCraftingRecipe),
        )

        Registry.register(BuiltInRegistries.BLOCK, MCIdentifier(JavaLuckyRegistry.blockId), FabricLuckyRegistry.luckyBlock)
        Registry.register(BuiltInRegistries.ITEM, MCIdentifier(JavaLuckyRegistry.blockId), FabricLuckyRegistry.luckyBlockItem)
        Registry.register(BuiltInRegistries.ITEM, MCIdentifier(JavaLuckyRegistry.bowId), FabricLuckyRegistry.luckyBow)
        Registry.register(BuiltInRegistries.ITEM, MCIdentifier(JavaLuckyRegistry.swordId), FabricLuckyRegistry.luckySword)
        Registry.register(BuiltInRegistries.ITEM, MCIdentifier(JavaLuckyRegistry.potionId), FabricLuckyRegistry.luckyPotion)

        JavaLuckyRegistry.addons.map { addon ->
            if (addon.ids.block != null) {
                val block = LuckyBlock()
                Registry.register(BuiltInRegistries.BLOCK, MCIdentifier(addon.ids.block!!), block)
                Registry.register(BuiltInRegistries.ITEM, MCIdentifier(addon.ids.block!!), LuckyBlockItem(block))
                //registerWorldGen(addon.ids.block!!)
            }
            if (addon.ids.bow != null) Registry.register(BuiltInRegistries.ITEM, MCIdentifier(addon.ids.bow!!), LuckyBow())
            if (addon.ids.sword != null) Registry.register(BuiltInRegistries.ITEM, MCIdentifier(addon.ids.sword!!), LuckySword())
            if (addon.ids.potion != null) Registry.register(BuiltInRegistries.ITEM, MCIdentifier(addon.ids.potion!!), LuckyPotion())
        }

        registerWorldGen()
        registerAddonCraftingRecipes()
        setupCreativeTabs()
    }
}

@OnlyInClient
class FabricModClient : ClientModInitializer {
    override fun onInitializeClient() {
        for (addon in JavaLuckyRegistry.addons) {
            val addonModMetadata = object : ModMetadata {
                override fun getType(): String = "builtin"
                override fun getId(): String = "${addon.addonId}_resources"
                override fun getProvides(): MutableCollection<String> = mutableListOf()
                override fun getVersion(): Version = Version.parse("1.0.0")
                override fun getEnvironment(): ModEnvironment = ModEnvironment.CLIENT
                override fun getDependencies(): MutableCollection<ModDependency> = mutableListOf()
                override fun getName(): String = "${addon.addonId} resources"
                override fun getDescription(): String = ""
                override fun getAuthors(): MutableCollection<Person> = mutableListOf()
                override fun getContributors(): MutableCollection<Person> = mutableListOf()
                override fun getContact(): ContactInformation = ContactInformation.EMPTY
                override fun getLicense(): MutableCollection<String> = mutableListOf()
                override fun getIconPath(size: Int): Optional<String> = Optional.empty()
                override fun containsCustomValue(key: String?): Boolean = false
                override fun getCustomValue(key: String?): CustomValue = throw Exception()
                override fun getCustomValues(): MutableMap<String, CustomValue> = mutableMapOf()
                @Deprecated("") override fun containsCustomElement(key: String?): Boolean = false
            }

            val addonModContainer = object : ModContainer {
                override fun getMetadata(): ModMetadata = addonModMetadata
                override fun getOrigin(): ModOrigin = ModOriginImpl(mutableListOf(addon.file.toPath()))
                override fun getRootPaths(): MutableList<Path> {
                    if (addon.file.isDirectory) return mutableListOf(addon.file.toPath())

                    val fileSystem = FileSystemUtil.getJarFileSystem(addon.file.toPath(), false).get()
                    return mutableListOf(fileSystem.rootDirectories.iterator().next())
                }

                override fun getContainingMod(): Optional<ModContainer> = Optional.empty()
                override fun getContainedMods(): MutableCollection<ModContainer> = mutableListOf()
                @Deprecated("") override fun getRootPath(): Path = rootPaths.first()
                @Deprecated("") override fun getPath(file: String?): Path = findPath(file).get()
            }

            ResourceManagerHelperImpl.registerBuiltinResourcePack(
                MCIdentifier("lucky", "${addon.addonId.substring("lucky:".length)}_resources"),
                "",
                addonModContainer,
                ResourcePackActivationType.ALWAYS_ENABLED
            )
        }

        registerLuckyBowModels(FabricLuckyRegistry.luckyBow)
        JavaLuckyRegistry.addons.map { addon ->
            addon.ids.bow?.let {
                registerLuckyBowModels(BuiltInRegistries.ITEM.get(MCIdentifier(it)) as LuckyBow)
            }
        }

        EntityRendererRegistry.register(FabricLuckyRegistry.luckyProjectile) { ctx ->
            LuckyProjectileRenderer(ctx)
        }
        EntityRendererRegistry.register(FabricLuckyRegistry.thrownLuckyPotion) { ctx ->
            ThrownLuckyPotionRenderer(ctx)
        }
        EntityRendererRegistry.register(FabricLuckyRegistry.delayedDrop) { ctx ->
            DelayedDropRenderer(ctx)
        }
    }
}
