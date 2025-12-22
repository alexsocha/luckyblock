package mod.lucky.neoforge

import com.mojang.logging.LogUtils
import com.mojang.serialization.MapCodec
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.java.JAVA_GAME_API
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JavaPlatformAPI
import mod.lucky.neoforge.ForgeLuckyRegistry.luckyProjectile
import mod.lucky.neoforge.game.*
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceKey
import net.minecraft.server.packs.FilePackResources.FileResourcesSupplier
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.PathPackResources.PathResourcesSupplier
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.event.AddPackFindersEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.*
import kotlin.collections.HashMap


object ForgeLuckyRegistry {
    val LOGGER = LogUtils.getLogger();

    const val modId = "lucky"
    lateinit var modVersion: String
    val addonBlocks = HashMap<String, DeferredBlock<LuckyBlock>>()
    val addonItems = HashMap<String, DeferredItem<*>>()

    val blockRegistry = DeferredRegister.createBlocks(modId)
    val itemRegistry = DeferredRegister.createItems(modId)
    val blockTypeRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, modId)
    val blockEntityTypeRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, modId)
    val entityTypeRegistry = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, modId)
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
            val validBlocks = listOf(luckyBlock.get()) + JavaLuckyRegistry.addons
                .mapNotNull { it.ids.block }
                .map { addonBlocks[it]!!.get() }
            BuiltInRegistries.DATA_COMPONENT_TYPE
            BlockEntityType(::LuckyBlockEntity, validBlocks.toSet())
        }
    )

    val luckyProjectile = entityTypeRegistry.register(MCIdentifier.parse(JavaLuckyRegistry.projectileId).path) { id ->
        EntityType.Builder.of(::LuckyProjectile, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
    }

    val luckyBow = itemRegistry.register(
        MCIdentifier.parse(JavaLuckyRegistry.bowId).path,
        { id -> LuckyBow(id) }
    )

    val luckyBiomeModifierSerializer = biomeModifierRegistry.register(
        "lucky_biome_modifier",
        { _ -> MapCodec.unit(LuckyBiomeModifier.INSTANCE) }
    )

    val luckModifierCraftingRecipe = recipeRegistry.register(
        "crafting_luck",
        { _ -> CustomRecipe.Serializer(::LuckModifierCraftingRecipe) }
    )

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

fun registerAddons() {
    JavaLuckyRegistry.addons.map { addon ->
        if (addon.ids.block != null) {
            val blockId = addon.ids.block!!;
            ForgeLuckyRegistry.addonBlocks[blockId] =
                ForgeLuckyRegistry.blockRegistry.register(MCIdentifier.parse(blockId).path) {
                    id -> LuckyBlock(id)
                }

            ForgeLuckyRegistry.addonItems[blockId] =
                ForgeLuckyRegistry.itemRegistry.register(MCIdentifier.parse(blockId).path) {
                    id -> LuckyBlockItem(ForgeLuckyRegistry.addonBlocks[blockId]!!.get(), id)
                }
        }
        addon.ids.bow?.let {
            ForgeLuckyRegistry.addonItems[it] =
                ForgeLuckyRegistry.itemRegistry.register(MCIdentifier.parse(it).path, { id -> LuckyBow(id) })
        }
        /*
        if (addon.ids.sword != null) ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.sword!!).path) { LuckySword() }
        if (addon.ids.potion != null) ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.potion!!).path) { LuckyPotion() }
         */
    }
}

class CommonModEvents {
    @SubscribeEvent // on the mod event bus
    fun buildContents(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ForgeLuckyRegistry.luckyBlockItem)
            createLuckySubItems(
                ForgeLuckyRegistry.luckyBlockItem.get(),
                event.parameters.holders
            ).forEach { event.accept(it) }
        }
        if (event.tabKey.equals(CreativeModeTabs.COMBAT)) {
            //event.accept(ForgeLuckyRegistry.luckySword)
            event.accept(ForgeLuckyRegistry.luckyBow)
            //event.accept(ForgeLuckyRegistry.luckyPotion)
            //createLuckySubItems(ForgeLuckyRegistry.luckyPotion.get(), LuckyItemValues.veryLuckyPotion, LuckyItemValues.veryUnluckyPotion).forEach { event.accept(it) }
        }

        for (addon in JavaLuckyRegistry.addons) {
            if (event.tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
                if (addon.ids.block != null) event.accept {
                    ForgeLuckyRegistry.addonItems[addon.ids.block]!!.get()
                }
            }
            if (event.tabKey.equals(CreativeModeTabs.COMBAT)) {
                //if (addon.ids.sword != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.sword!!))!! }
                if (addon.ids.bow != null) event.accept { ForgeLuckyRegistry.addonItems[addon.ids.bow]!!.get() }
                //if (addon.ids.potion != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.potion!!))!! }
            }
        }
    }
}

@Mod("lucky")
class ForgeMod(modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {
        @EventBusSubscriber(modid = "lucky", bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            private fun registerEntityRenderers(event: RegisterRenderers) {
                event.registerEntityRenderer(luckyProjectile.get(), ::LuckyProjectileRenderer)
            }

            @SubscribeEvent
            private fun onAddPackFinders(event: AddPackFindersEvent) {
                JavaLuckyRegistry.addons.forEach { addon ->
                    val packName = "Resources for ${addon.addonId}"
                    val packSupplier = if (addon.file.isDirectory) PathResourcesSupplier(addon.file.toPath())
                    else FileResourcesSupplier(addon.file)

                    // based on net.minecraftforge.client.loading.ClientModLoader
                    val repositorySource = RepositorySource { packConsumer ->
                        val packWithMeta = Pack.readMetaAndCreate(
                            PackLocationInfo(
                                packName,
                                MCChatComponent.literal(packName),
                                PackSource.DEFAULT,
                                Optional.empty()
                            ),
                            packSupplier,
                            PackType.CLIENT_RESOURCES,
                            PackSelectionConfig(
                                true,
                                Pack.Position.BOTTOM,
                                true,
                            )
                        )
                        packConsumer.accept(packWithMeta)
                    }
                    event.addRepositorySource(repositorySource)
                }
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
        registerAddons()

        ForgeLuckyRegistry.blockRegistry.register(modEventBus)
        ForgeLuckyRegistry.blockTypeRegistry.register(modEventBus)
        ForgeLuckyRegistry.blockEntityTypeRegistry.register(modEventBus)
        ForgeLuckyRegistry.itemRegistry.register(modEventBus)
        ForgeLuckyRegistry.entityTypeRegistry.register(modEventBus)
        ForgeLuckyRegistry.biomeModifierRegistry.register(modEventBus)
        ForgeLuckyRegistry.recipeRegistry.register(modEventBus)
        ForgeLuckyRegistry.dataComponentTypeRegistry.register(modEventBus)
        modEventBus.register(CommonModEvents())
    }
}
