package mod.lucky.fabric

import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.java.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.resource.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import mod.lucky.fabric.game.*
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.util.registry.BuiltinRegistries

import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.gen.GenerationStep

import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature

import net.minecraft.world.gen.feature.FeatureConfig

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
        val configuredFeature = feature.configure(FeatureConfig.DEFAULT)
        val featureId = "${blockId}_world_gen"

        Registry.register<Feature<*>, Feature<*>>(Registry.FEATURE, Identifier(featureId), feature)
        val configuredId = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, MCIdentifier(featureId))
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, configuredId.value, configuredFeature)
        BiomeModifications.addFeature(BiomeSelectors.all(), GenerationStep.Feature.SURFACE_STRUCTURES, configuredId)
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
        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load {
                _, _ -> JavaLuckyRegistry.notificationState = checkForUpdates(
            JavaLuckyRegistry.notificationState)
        })

        ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
            for (addon in JavaLuckyRegistry.addons) {
                val file = addon.file
                val pack = if (file.isDirectory) DirectoryResourcePack(file) else ZipResourcePack(file)
                val resourceManager = MinecraftClient.getInstance().resourceManager
                if (resourceManager is ReloadableResourceManagerImpl) {
                    resourceManager.addPack(pack)
                }
            }
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
