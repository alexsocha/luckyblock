package mod.lucky.forge

import com.mojang.serialization.Codec
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.forge.game.*
import mod.lucky.java.*
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.FolderPackResources
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.crafting.SimpleRecipeSerializer
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.event.world.WorldEvent
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
    val blockEntityRegistry = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, modId)
    val entityRegistry = DeferredRegister.create(ForgeRegistries.ENTITIES, modId)
    val recipeRegistry = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modId)
    val biomeModifierSerializerRegistry = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, modId)
    val biomeModifierRegistry = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, modId)

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

        @Suppress BlockEntityType.Builder.of(::LuckyBlockEntity, *validBlocks.toTypedArray()).build(null)
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
        SimpleRecipeSerializer(::LuckModifierCraftingRecipe)
    }
    val addonCraftingRecipe = recipeRegistry.register("crafting_addons") {
        registerAddonCraftingRecipes()
        SimpleRecipeSerializer(::AddonCraftingRecipe)
    }

    val luckyBiomeModifierSerializer = biomeModifierSerializerRegistry.register("lucky_biome_modifier") {
        Codec.unit(LuckyBiomeModifier.INSTANCE)
    }
    val luckyBiomeModifier = biomeModifierRegistry.register("lucky_biome_modifier") {
        LuckyBiomeModifier.INSTANCE
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

@OnlyInClient
private fun setupClient() {
    MinecraftForge.EVENT_BUS.addListener { _: WorldEvent.Load ->
        JavaLuckyRegistry.notificationState = checkForUpdates(JavaLuckyRegistry.notificationState)
    }

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
        val pack = if (addon.file.isDirectory) FolderPackResources(addon.file) else FilePackResources(addon.file)

        // based on net.minecraftforge.client.loading.ClientModLoader
        val repositorySource = RepositorySource { packConsumer, packConstructor ->
            val packWithMeta = Pack.create(
                "Resources for ${addon.addonId}",
                true, // is included by default
                { pack },
                packConstructor,
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
        ForgeLuckyRegistry.biomeModifierRegistry.register(eventBus)

        eventBus.addListener { _: FMLClientSetupEvent -> setupClient() }
        eventBus.addListener(::registerEntityRenderers)
        eventBus.addListener(::addPackFinders)
    }
}
