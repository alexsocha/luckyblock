package mod.lucky.fabric

import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.fabric.game.*
import mod.lucky.java.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.*
import net.fabricmc.loader.impl.metadata.ModOriginImpl
import net.fabricmc.loader.impl.util.FileSystemUtil
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.*
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
    val spawnPacketId = Identifier("lucky:spawn_packet")
    lateinit var luckyBlockEntity: BlockEntityType<LuckyBlockEntity>
    lateinit var luckyProjectile: EntityType<LuckyProjectile>
    lateinit var thrownLuckyPotion: EntityType<ThrownLuckyPotion>
    lateinit var delayedDrop: EntityType<DelayedDrop>
    lateinit var luckModifierCraftingRecipe: SpecialRecipeSerializer<LuckModifierCraftingRecipe>
    lateinit var addonCraftingRecipe: SpecialRecipeSerializer<AddonCraftingRecipe>
}

class FabricMod : ModInitializer {
    init {
        PLATFORM_API = JavaPlatformAPI
        LOGGER = FabricGameAPI
        GAME_API = FabricGameAPI
        JAVA_GAME_API = FabricJavaGameAPI
    }

    private fun registerWorldGen(blockId: String) {
        val feature = LuckyWorldFeature(DefaultFeatureConfig.CODEC, blockId)
        val placedFeature = PlacedFeature(
            RegistryEntry.of(ConfiguredFeature(feature, DefaultFeatureConfig())),
            emptyList()
        )
        val featureId = "${blockId}_world_gen"

        Registry.register<Feature<*>, Feature<*>>(Registry.FEATURE, Identifier(featureId), feature)
        val placedId = RegistryKey.of(Registry.PLACED_FEATURE_KEY, MCIdentifier(featureId))
        Registry.register(BuiltinRegistries.PLACED_FEATURE, placedId.value, placedFeature)
        BiomeModifications.addFeature(BiomeSelectors.all(), GenerationStep.Feature.SURFACE_STRUCTURES, placedId)
    }

    override fun onInitialize() {
        FabricGameAPI.init()
        JavaLuckyRegistry.init()

        FabricLuckyRegistry.luckyBlockEntity = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            JavaLuckyRegistry.blockId,
            BlockEntityType.Builder.create(::LuckyBlockEntity, FabricLuckyRegistry.luckyBlock).build(null)
        )
        FabricLuckyRegistry.luckyProjectile = Registry.register(
            Registry.ENTITY_TYPE,
            JavaLuckyRegistry.projectileId,
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::LuckyProjectile)
                .trackRangeChunks(100)
                .trackedUpdateRate(20)
                .forceTrackedVelocityUpdates(true)
                .build()
        )
        FabricLuckyRegistry.thrownLuckyPotion = Registry.register(
            Registry.ENTITY_TYPE,
            JavaLuckyRegistry.potionId,
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::ThrownLuckyPotion)
                .trackRangeChunks(100)
                .trackedUpdateRate(20)
                .forceTrackedVelocityUpdates(true)
                .build()
        )

        FabricLuckyRegistry.delayedDrop = Registry.register(
            Registry.ENTITY_TYPE,
            JavaLuckyRegistry.delayedDropId,
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::DelayedDrop).build()
        )

        FabricLuckyRegistry.luckModifierCraftingRecipe = Registry.register(
            Registry.RECIPE_SERIALIZER,
            MCIdentifier("lucky:crafting_luck"),
            SpecialRecipeSerializer(::LuckModifierCraftingRecipe),
        )
        FabricLuckyRegistry.addonCraftingRecipe = Registry.register(
            Registry.RECIPE_SERIALIZER,
            MCIdentifier("lucky:crafting_addons"),
            SpecialRecipeSerializer(::AddonCraftingRecipe),
        )

        Registry.register(Registry.BLOCK, Identifier(JavaLuckyRegistry.blockId), FabricLuckyRegistry.luckyBlock)
        Registry.register(Registry.ITEM, Identifier(JavaLuckyRegistry.blockId), FabricLuckyRegistry.luckyBlockItem)
        Registry.register(Registry.ITEM, Identifier(JavaLuckyRegistry.bowId), FabricLuckyRegistry.luckyBow)
        Registry.register(Registry.ITEM, Identifier(JavaLuckyRegistry.swordId), FabricLuckyRegistry.luckySword)
        Registry.register(Registry.ITEM, Identifier(JavaLuckyRegistry.potionId), FabricLuckyRegistry.luckyPotion)
        registerWorldGen(JavaLuckyRegistry.blockId)

        JavaLuckyRegistry.addons.map { addon ->
            if (addon.ids.block != null) {
                val block = LuckyBlock()
                Registry.register(Registry.BLOCK, Identifier(addon.ids.block!!), block)
                Registry.register(Registry.ITEM, Identifier(addon.ids.block!!), LuckyBlockItem(block))
                registerWorldGen(addon.ids.block!!)
            }
            if (addon.ids.bow != null) Registry.register(Registry.ITEM, Identifier(addon.ids.bow!!), LuckyBow())
            if (addon.ids.sword != null) Registry.register(Registry.ITEM, Identifier(addon.ids.sword!!), LuckySword())
            if (addon.ids.potion != null) Registry.register(Registry.ITEM, Identifier(addon.ids.potion!!), LuckyPotion())
        }

        registerAddonCraftingRecipes()
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

        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load {
                _, _ -> JavaLuckyRegistry.notificationState = checkForUpdates(
            JavaLuckyRegistry.notificationState)
        })


        registerLuckyBowModels(FabricLuckyRegistry.luckyBow)
        JavaLuckyRegistry.addons.map { addon ->
            addon.ids.bow?.let {
                registerLuckyBowModels(Registry.ITEM.get(Identifier(it)) as LuckyBow)
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

        ClientPlayNetworking.registerGlobalReceiver(FabricLuckyRegistry.spawnPacketId) { client, _, buf, _ ->
        val spawnPacket = SpawnPacket.decode(buf)
            val world = client.world
            if (spawnPacket != null && world != null) {
                client.execute { spawnPacket.execute(world) }
            }
        }
    }
}
