package mod.lucky;

import java.util.ArrayList;

import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.init.SetupClient;
import mod.lucky.init.SetupCommon;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import mod.lucky.structure.Structure;
import mod.lucky.world.LuckyTickHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.versions.mcp.MCPVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod("lucky")
public class Lucky {
    public static String VERSION;
    public static final String MC_VERSION = MCPVersion.getMCVersion();

    public static final Logger LOGGER = LogManager.getLogger();

    public static BlockLuckyBlock luckyBlock;
    public static ItemLuckySword luckySword;
    public static ItemLuckyBow luckyBow;
    public static ItemLuckyPotion luckyPotion;

    public static ArrayList<PluginLoader> luckyBlockPlugins;
    public static ArrayList<Structure> structures;

    public static ResourceManager resourceManager;
    public static LuckyTickHandler tickHandler;

    public Lucky() {
        Lucky.VERSION = ModLoadingContext.get().getActiveContainer().getModInfo()
            .getVersion().toString();

        SetupCommon.setupStatic();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupCommon(final FMLCommonSetupEvent event) { SetupCommon.setup(); }
    private void setupClient(final FMLClientSetupEvent event) { SetupClient.setup(); }

    public static void error(@Nullable Exception e, String message) {
        LOGGER.error(message);
        if (e != null) e.printStackTrace();
    }
}
