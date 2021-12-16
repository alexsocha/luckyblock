package mod.lucky.forge

import mod.lucky.common.GAME_API
import mod.lucky.common.PLATFORM_API
import mod.lucky.common.LOGGER
import mod.lucky.java.JavaPlatformAPI
import mod.lucky.forge.game.*
import mod.lucky.java.*
import net.minecraft.client.Minecraft
import net.minecraft.resources.*
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.FolderPackResources
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.SimpleRecipeSerializer
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.world.BiomeLoadingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ForgeLuckyRegistry {
    val LOGGER: Logger = LogManager.getLogger()
    val luckyBlock = LuckyBlock()
    val addonLuckyBlocks = HashMap<String, LuckyBlock>()
    val luckyBlockItem = LuckyBlockItem(luckyBlock)
    val luckyBow = LuckyBow()
    val luckySword = LuckySword()
    val luckyPotion = LuckyPotion()
    lateinit var modVersion: String
    lateinit var luckyBlockEntity: BlockEntityType<LuckyBlockEntity>
    lateinit var luckyProjectile: EntityType<LuckyProjectile>
    lateinit var thrownLuckyPotion: EntityType<ThrownLuckyPotion>
    lateinit var delayedDrop: EntityType<DelayedDrop>
    lateinit var luckModifierCraftingRecipe: RecipeSerializer<LuckModifierCraftingRecipe>
    lateinit var addonCraftingRecipe: RecipeSerializer<AddonCraftingRecipe>
}

private fun getAddonBlock(id: String): LuckyBlock {
    return ForgeLuckyRegistry.addonLuckyBlocks.getOrElse(id, {
        val block = LuckyBlock()
        ForgeLuckyRegistry.addonLuckyBlocks[id] = block
        block
    })
}

@Mod("lucky")
class ForgeMod {
    init {
        PLATFORM_API = JavaPlatformAPI
        LOGGER = ForgeGameAPI
        GAME_API = ForgeGameAPI
        JAVA_GAME_API = ForgeJavaGameAPI

        ForgeLuckyRegistry.modVersion = ModLoadingContext.get().activeContainer.modInfo.version.toString()

        ForgeGameAPI.init()
        JavaLuckyRegistry.init()

        FMLJavaModLoadingContext.get().modEventBus
            .addListener { _: FMLCommonSetupEvent -> setupCommon() }
        FMLJavaModLoadingContext.get().modEventBus
            .addListener{ _: FMLClientSetupEvent -> setupClient() }
        MinecraftForge.EVENT_BUS.addListener { event: BiomeLoadingEvent ->
            registerBiomeFeatures(event)
        }
        MinecraftForge.EVENT_BUS.register(this)
    }

    private fun registerBiomeFeatures(event: BiomeLoadingEvent) {
        val blockIds = listOf(JavaLuckyRegistry.blockId) + JavaLuckyRegistry.addons.mapNotNull { it.ids.block }
        blockIds.forEach {
            val feature = LuckyWorldFeature(NoneFeatureConfiguration.CODEC, it)
            val configuredFeature = feature.configured(NoneFeatureConfiguration.INSTANCE)
            event.generation.getFeatures(GenerationStep.Decoration.SURFACE_STRUCTURES).add { configuredFeature }
        }
    }

    private fun setupCommon() {
        registerAddonCraftingRecipes()
    }

    @OnlyInClient
    private fun setupClient() {
        MinecraftForge.EVENT_BUS.addListener { _: WorldEvent.Load ->
            JavaLuckyRegistry.notificationState = checkForUpdates(JavaLuckyRegistry.notificationState)
        }

        for (addon in JavaLuckyRegistry.addons) {
            val file = addon.file
            val pack = if (file.isDirectory) FolderPackResources(file) else FilePackResources(file)
            val resourceManager = Minecraft.getInstance().resourceManager
            if (resourceManager is SimpleReloadableResourceManager) {
                resourceManager.add(pack)
            }
        }

        registerLuckyBowModels(ForgeLuckyRegistry.luckyBow)
        JavaLuckyRegistry.addons.map { addon ->
            if (addon.ids.bow != null) registerLuckyBowModels(ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.bow!!)) as LuckyBow)
        }
    }
}

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object ForgeSubscriber {
    @JvmStatic @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<MCBlock>) {
        event.registry.register(ForgeLuckyRegistry.luckyBlock.setRegistryName(JavaLuckyRegistry.blockId))
        JavaLuckyRegistry.addons.map { addon ->
            if (addon.ids.block != null) {
                event.registry.register(getAddonBlock(addon.ids.block!!).setRegistryName(addon.ids.block))
            }
        }
    }

    @JvmStatic @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<MCItem>) {
        event.registry.register(ForgeLuckyRegistry.luckyBlockItem.setRegistryName(JavaLuckyRegistry.blockId))
        event.registry.register(ForgeLuckyRegistry.luckySword.setRegistryName(JavaLuckyRegistry.swordId))
        event.registry.register(ForgeLuckyRegistry.luckyBow.setRegistryName(JavaLuckyRegistry.bowId))
        event.registry.register(ForgeLuckyRegistry.luckyPotion.setRegistryName(JavaLuckyRegistry.potionId))

        JavaLuckyRegistry.addons.map { addon ->
            if (addon.ids.block != null) {
                val block = ForgeLuckyRegistry.addonLuckyBlocks.getOrElse(addon.ids.block!!, { LuckyBlock() })
                event.registry.register(LuckyBlockItem(block).setRegistryName(block.registryName))
            }
            if (addon.ids.sword != null) event.registry.register(LuckySword().setRegistryName(addon.ids.sword))
            if (addon.ids.bow != null) event.registry.register(LuckyBow().setRegistryName(addon.ids.bow))
            if (addon.ids.potion != null) event.registry.register(LuckyPotion().setRegistryName(addon.ids.potion))
        }
    }

    @JvmStatic @SubscribeEvent
    fun registerEntites(event: RegistryEvent.Register<EntityType<*>>) {
        ForgeLuckyRegistry.luckyProjectile = EntityType.Builder.of(::LuckyProjectile, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.projectileId)

        ForgeLuckyRegistry.thrownLuckyPotion = EntityType.Builder.of(::ThrownLuckyPotion, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.potionId)

        ForgeLuckyRegistry.delayedDrop = EntityType.Builder.of(::DelayedDrop, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.potionId)

        event.registry.register(ForgeLuckyRegistry.luckyProjectile.setRegistryName(JavaLuckyRegistry.projectileId))
        event.registry.register(ForgeLuckyRegistry.thrownLuckyPotion.setRegistryName(JavaLuckyRegistry.potionId))
        event.registry.register(ForgeLuckyRegistry.delayedDrop.setRegistryName(JavaLuckyRegistry.delayedDropId))
    }

    @JvmStatic @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<RecipeSerializer<*>>) {
        ForgeLuckyRegistry.luckModifierCraftingRecipe = SimpleRecipeSerializer(::LuckModifierCraftingRecipe)
        ForgeLuckyRegistry.addonCraftingRecipe = SimpleRecipeSerializer(::AddonCraftingRecipe)

        event.registry.register(ForgeLuckyRegistry.luckModifierCraftingRecipe.setRegistryName(ResourceLocation("lucky:crafting_luck")))
        event.registry.register(ForgeLuckyRegistry.addonCraftingRecipe.setRegistryName(ResourceLocation("lucky:crafting_addons")))
    }

    @JvmStatic @SubscribeEvent
    fun registerTileEntites(event: RegistryEvent.Register<BlockEntityType<*>>) {
        val validBlocks = listOf(ForgeLuckyRegistry.luckyBlock) + JavaLuckyRegistry.addons
            .mapNotNull { it.ids.block }
            .map { getAddonBlock(it) }

        ForgeLuckyRegistry.luckyBlockEntity = @Suppress BlockEntityType.Builder.of(::LuckyBlockEntity, *validBlocks.toTypedArray()).build(null)

        event.registry.register(ForgeLuckyRegistry.luckyBlockEntity.setRegistryName(JavaLuckyRegistry.blockId))
    }
}

@EventBusSubscriber(Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
object ForgeClientSubscriber {
    @JvmStatic @SubscribeEvent
    fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(ForgeLuckyRegistry.luckyProjectile, ::LuckyProjectileRenderer)
        event.registerEntityRenderer(ForgeLuckyRegistry.thrownLuckyPotion, ::ThrownLuckyPotionRenderer)
        event.registerEntityRenderer(ForgeLuckyRegistry.delayedDrop, ::DelayedDropRenderer)
    }
}
