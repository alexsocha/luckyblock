package mod.lucky.forge

import com.mojang.logging.LogUtils
import mod.lucky.common.GAME_API
import mod.lucky.common.LOGGER
import mod.lucky.common.PLATFORM_API
import mod.lucky.java.JAVA_GAME_API
import mod.lucky.java.JavaGameAPI
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JavaPlatformAPI
import net.minecraft.client.Minecraft
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.neoforged.neoforge.registries.NeoForgeRegistriesSetup
import net.neoforged.neoforge.registries.RegisterEvent

object ForgeLuckyRegistry {
    val LOGGER = LogUtils.getLogger();

    const val modId = "lucky"
    lateinit var modVersion: String

    val blockRegistry = DeferredRegister.createBlocks(modId)
}

@Mod("lucky")
class ForgeMod(modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {

        // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(modid = "lucky", bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            fun onClientSetup(event: FMLClientSetupEvent?) {
                // Some client setup code
                //LOGGER.info("HELLO FROM CLIENT SETUP")
                //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
            }
        }
    }

    init {
        PLATFORM_API = JavaPlatformAPI
        GAME_API = ForgeGameAPI
        LOGGER = ForgeGameAPI
        JAVA_GAME_API = ForgeJavaGameAPI

        ForgeLuckyRegistry.blockRegistry.register(modEventBus)
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    private fun registerEvent(event: RegisterEvent) {
        (GAME_API as ForgeGameAPI).initRegistry(event.registry)
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        ForgeLuckyRegistry.LOGGER.info("HELLO from server starting")
    }
}
