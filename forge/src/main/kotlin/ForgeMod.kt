package mod.lucky.forge

import com.mojang.serialization.Codec
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.forge.game.*
import mod.lucky.java.*
import mod.lucky.java.game.LuckyItemValues
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.PathPackResources
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ForgeLuckyRegistry {
    const val modId = "lucky"
    lateinit var modVersion: String

    val blockRegistry = DeferredRegister.create(ForgeRegistries.BLOCKS, modId)
    val itemRegistry = DeferredRegister.create(ForgeRegistries.ITEMS, modId)
    val blockEntityRegistry = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, modId)
    val entityRegistry = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, modId)
    val recipeRegistry = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modId)
    val biomeModifierSerializerRegistry = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, modId)

    val LOGGER: Logger = LogManager.getLogger()
    val addonLuckyBlocks = HashMap<String, LuckyBlock>()

    val luckyBlock = blockRegistry.register(MCIdentifier(JavaLuckyRegistry.blockId).path) { LuckyBlock() }
    val luckyBlockItem =
        itemRegistry.register(MCIdentifier(JavaLuckyRegistry.blockId).path) { LuckyBlockItem(luckyBlock.get()) }

    val luckySword = itemRegistry.register(MCIdentifier(JavaLuckyRegistry.swordId).path) { LuckySword() }
    val luckyBow = itemRegistry.register(MCIdentifier(JavaLuckyRegistry.bowId).path) { LuckyBow() }
    val luckyPotion = itemRegistry.register(MCIdentifier(JavaLuckyRegistry.potionId).path) { LuckyPotion() }

    val luckyBlockEntity = blockEntityRegistry.register(MCIdentifier(JavaLuckyRegistry.blockId).path) {
        val validBlocks = listOf(luckyBlock.get()) + JavaLuckyRegistry.addons
            .mapNotNull { it.ids.block }
            .map { getOrCreateAddonBlock(it) }

        BlockEntityType.Builder.of(::LuckyBlockEntity, *validBlocks.toTypedArray()).build(null)
    }

    val luckyProjectile = entityRegistry.register(MCIdentifier(JavaLuckyRegistry.projectileId).path) {
        EntityType.Builder.of(::LuckyProjectile, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.projectileId)
    }

    val thrownLuckyPotion = entityRegistry.register(MCIdentifier(JavaLuckyRegistry.potionId).path) {
        EntityType.Builder.of(::ThrownLuckyPotion, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.potionId)
    }

    val delayedDrop = entityRegistry.register(MCIdentifier(JavaLuckyRegistry.delayedDropId).path) {
        EntityType.Builder.of(::DelayedDrop, MobCategory.MISC)
            .setTrackingRange(100)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build(JavaLuckyRegistry.potionId)
    }

    val luckModifierCraftingRecipe = recipeRegistry.register("crafting_luck") {
        SimpleCraftingRecipeSerializer(::LuckModifierCraftingRecipe)
    }
    val addonCraftingRecipe = recipeRegistry.register("crafting_addons") {
        registerAddonCraftingRecipes()
        SimpleCraftingRecipeSerializer(::AddonCraftingRecipe)
    }

    val luckyBiomeModifierSerializer = biomeModifierSerializerRegistry.register("lucky_biome_modifier") {
        Codec.unit(LuckyBiomeModifier.INSTANCE)
    }
}

private fun getOrCreateAddonBlock(id: String): LuckyBlock {
    return ForgeLuckyRegistry.addonLuckyBlocks.getOrElse(id) {
        val block = LuckyBlock()
        ForgeLuckyRegistry.addonLuckyBlocks[id] = block
        block
    }
}

fun registerAddons() {
    JavaLuckyRegistry.addons.map { addon ->
        if (addon.ids.block != null) {
            ForgeLuckyRegistry.blockRegistry.register(MCIdentifier(addon.ids.block!!).path) { getOrCreateAddonBlock(addon.ids.block!!) }
            ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.block!!).path) {
                LuckyBlockItem(getOrCreateAddonBlock(addon.ids.block!!))
            }
        }
        if (addon.ids.sword != null) ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.sword!!).path) { LuckySword() }
        if (addon.ids.bow != null) ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.bow!!).path) { LuckyBow() }
        if (addon.ids.potion != null) ForgeLuckyRegistry.itemRegistry.register(MCIdentifier(addon.ids.potion!!).path) { LuckyPotion() }
    }
}

fun setupCreativeTabs(event: BuildCreativeModeTabContentsEvent) {
    if (event.tabKey.equals(CreativeModeTabs.BUILDING_BLOCKS)) {
        event.accept(ForgeLuckyRegistry.luckyBlockItem)
        createLuckySubItems(ForgeLuckyRegistry.luckyBlockItem.get(), LuckyItemValues.veryLuckyBlock, LuckyItemValues.veryUnluckyBlock).forEach { event.accept(it) }
    }
    if (event.tabKey.equals(CreativeModeTabs.COMBAT)) {
        event.accept(ForgeLuckyRegistry.luckySword)
        event.accept(ForgeLuckyRegistry.luckyBow)
        event.accept(ForgeLuckyRegistry.luckyPotion)
        createLuckySubItems(ForgeLuckyRegistry.luckyPotion.get(), LuckyItemValues.veryLuckyPotion, LuckyItemValues.veryUnluckyPotion).forEach { event.accept(it) }
    }

    for (addon in JavaLuckyRegistry.addons) {
        if (event.tabKey.equals(CreativeModeTabs.BUILDING_BLOCKS)) {
            if (addon.ids.block != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.block!!))!! }
        }
        if (event.tabKey.equals(CreativeModeTabs.COMBAT)) {
            if (addon.ids.sword != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.sword!!))!! }
            if (addon.ids.bow != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.bow!!))!! }
            if (addon.ids.potion != null) event.accept { ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.potion!!))!! }
        }
    }
}

@OnlyInClient
private fun setupClient() {
    registerLuckyBowModels(ForgeLuckyRegistry.luckyBow.get())
    JavaLuckyRegistry.addons.map { addon ->
        if (addon.ids.bow != null) registerLuckyBowModels(ForgeRegistries.ITEMS.getValue(MCIdentifier(addon.ids.bow!!)) as LuckyBow)
    }
}

@OnlyInClient
private fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
    event.registerEntityRenderer(ForgeLuckyRegistry.luckyProjectile.get(), ::LuckyProjectileRenderer)
    event.registerEntityRenderer(ForgeLuckyRegistry.thrownLuckyPotion.get(), ::ThrownLuckyPotionRenderer)
    event.registerEntityRenderer(ForgeLuckyRegistry.delayedDrop.get(), ::DelayedDropRenderer)
}

@OnlyInClient
private fun addPackFinders(event: AddPackFindersEvent) {
    JavaLuckyRegistry.addons.forEach { addon ->
        val packName = "Resources for ${addon.addonId}"
        val isBuiltIn = true;
        val pack = if (addon.file.isDirectory) PathPackResources(addon.addonId, addon.file.toPath(), isBuiltIn)
            else FilePackResources(packName, addon.file, isBuiltIn)

        // based on net.minecraftforge.client.loading.ClientModLoader
        val repositorySource = RepositorySource { packConsumer ->
            val packWithMeta = Pack.readMetaAndCreate(
                packName,
                MCChatComponent.literal(packName),
                isBuiltIn,
                { pack },
                PackType.CLIENT_RESOURCES,
                Pack.Position.BOTTOM,
                PackSource.DEFAULT
            )
            packConsumer.accept(packWithMeta)
        }
        event.addRepositorySource(repositorySource)
    }
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
        registerAddons()

        val eventBus = FMLJavaModLoadingContext.get().modEventBus
        ForgeLuckyRegistry.blockRegistry.register(eventBus)
        ForgeLuckyRegistry.itemRegistry.register(eventBus)
        ForgeLuckyRegistry.blockEntityRegistry.register(eventBus)
        ForgeLuckyRegistry.entityRegistry.register(eventBus)
        ForgeLuckyRegistry.recipeRegistry.register(eventBus)
        ForgeLuckyRegistry.biomeModifierSerializerRegistry.register(eventBus)

        eventBus.addListener { _: FMLClientSetupEvent -> setupClient() }
        eventBus.addListener(::setupCreativeTabs)
        eventBus.addListener(::registerEntityRenderers)
        eventBus.addListener(::addPackFinders)
    }
}
