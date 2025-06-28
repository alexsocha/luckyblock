package mod.lucky.neoforge

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.forge.game.LuckModifierCraftingRecipe
import mod.lucky.forge.game.LuckyBiomeModifier
import mod.lucky.forge.game.LuckyBlock
import mod.lucky.forge.game.LuckyBlockEntity
import mod.lucky.java.JAVA_GAME_API
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JavaPlatformAPI
import mod.lucky.java.game.LuckyItemValues
import mod.lucky.neoforge.game.LuckyBlockItem
import mod.lucky.neoforge.game.createLuckySubItems
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.neoforged.neoforge.registries.RegisterEvent


object ForgeLuckyRegistry {
    val LOGGER = LogUtils.getLogger();

    const val modId = "lucky"
    lateinit var modVersion: String

    val blockRegistry = DeferredRegister.createBlocks(modId)
    val itemRegistry = DeferredRegister.createItems(modId)
    val blockTypeRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, modId)
    val blockEntityTypeRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, modId)
    val biomeModifierRegistry = DeferredRegister.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, modId)
    val recipeRegistry = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, modId)
    val dataComponentTypeRegistry = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, modId)

    val luckyBlock = blockRegistry.register(
        MCIdentifier.parse(JavaLuckyRegistry.blockId).path,
        { id -> LuckyBlock(id) },
    )
    val luckyBlockItem = itemRegistry.register(
        MCIdentifier.parse(JavaLuckyRegistry.blockId).path,
        { id -> LuckyBlockItem(luckyBlock.get(), id) }
    )
    val luckyBlockEntity = blockEntityTypeRegistry.register(
        MCIdentifier.parse(JavaLuckyRegistry.blockId).path,
        { _ ->
            /* TODO
            val validBlocks = listOf(luckyBlock.get()) + JavaLuckyRegistry.addons
                .mapNotNull { it.ids.block }
                .map { getOrCreateAddonBlock(it) }
             */
            val validBlocks = listOf(luckyBlock.get())
            BuiltInRegistries.DATA_COMPONENT_TYPE
            BlockEntityType(::LuckyBlockEntity, validBlocks.toSet())
        }
    )

    val luckyBiomeModifierSerializer = biomeModifierRegistry.register(
        "lucky_biome_modifier",
        { _ -> MapCodec.unit(LuckyBiomeModifier.INSTANCE) }
    )

    val luckModifierCraftingRecipe = recipeRegistry.register(
        "crafting_luck",
        { _ -> CustomRecipe.Serializer(::LuckModifierCraftingRecipe) }
    )

    /*
    val addonCraftingRecipe = recipeRegistry.register("crafting_addons") {
        registerAddonCraftingRecipes()
        SimpleCraftingRecipeSerializer(AddonCraftingRecipe)
    }
     */

    val luckComponent = dataComponentTypeRegistry.register(
        "luck",
        { id ->
            DataComponentType.Builder<Int?>()
                .persistent(ExtraCodecs.intRange(-100, 100))
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build()
        }
    )
    val dropsComponent = dataComponentTypeRegistry.register(
        "drops",
        { id ->
            DataComponentType.Builder<List<String>>()
                .persistent(ExtraCodecs.ESCAPED_STRING.listOf())
                .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
                .build()
        }
    )
}

class CommonModEvents {
    @SubscribeEvent
    fun registerEvent(event: RegisterEvent) {
        ForgeLuckyRegistry.LOGGER.info("HELLO from registry")
        (GAME_API as ForgeGameAPI).initRegistry(event.registry)
    }

    @SubscribeEvent
    fun onServerStarting(event: FMLCommonSetupEvent) {
        ForgeLuckyRegistry.LOGGER.info("HELLO from server starting")
    }

    @SubscribeEvent // on the mod event bus
    fun buildContents(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ForgeLuckyRegistry.luckyBlockItem)
            createLuckySubItems(
                ForgeLuckyRegistry.luckyBlockItem.get(),
                event.parameters.holders
            ).forEach { event.accept(it) }
        }
    }
}

@Mod("lucky")
class ForgeMod(modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {
        @EventBusSubscriber(modid = "lucky", bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            fun onClientSetup(event: FMLClientSetupEvent?) {
                // Some client setup code
                ForgeLuckyRegistry.LOGGER.info("HELLO FROM CLIENT SETUP")
                //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
            }
        }
    }

    init {
        PLATFORM_API = JavaPlatformAPI
        GAME_API = ForgeGameAPI
        LOGGER = ForgeGameAPI
        JAVA_GAME_API = ForgeJavaGameAPI

        ForgeLuckyRegistry.modVersion = modContainer.modInfo.version.toString()

        ForgeGameAPI.init()
        JavaLuckyRegistry.init()
        //registerAddons() TODO

        ForgeLuckyRegistry.blockRegistry.register(modEventBus)
        ForgeLuckyRegistry.blockTypeRegistry.register(modEventBus)
        ForgeLuckyRegistry.blockEntityTypeRegistry.register(modEventBus)
        ForgeLuckyRegistry.itemRegistry.register(modEventBus)
        ForgeLuckyRegistry.biomeModifierRegistry.register(modEventBus)
        ForgeLuckyRegistry.recipeRegistry.register(modEventBus)
        ForgeLuckyRegistry.dataComponentTypeRegistry.register(modEventBus)
        modEventBus.register(CommonModEvents())
    }
}
