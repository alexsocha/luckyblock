package mod.lucky;

import java.util.ArrayList;

import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.init.SetupClient;
import mod.lucky.init.SetupCommon;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceRegistry;
import mod.lucky.structure.Structure;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.versions.mcp.MCPVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("lucky")
public class Lucky {
    private static String version;
    private static String mcversion = MCPVersion.getMCVersion();

    public static final Logger LOGGER = LogManager.getLogger();

    public static BlockLuckyBlock luckyBlock;
    public static ItemLuckySword luckySword;
    public static ItemLuckyBow luckyBow;
    public static ItemLuckyPotion luckyPotion;

    public static ArrayList<PluginLoader> luckyBlockPlugins;
    public static ArrayList<Structure> structures;

    public static ResourceRegistry resourceRegistry;
    public static LuckyTickHandler tickHandler;

    public static SimpleChannel networkChannel;



    public Lucky() {
        Lucky.version = ModLoadingContext.get().getActiveContainer().getModInfo()
            .getVersion().toString();
        LOGGER.info("==========" + Lucky.mcversion + ", " + Lucky.version);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        SetupCommon.setup();
    }

    private void setupClient(final FMLClientSetupEvent event) {
        SetupClient.setup();
    }

    public static Structure getStructure(String id) {
        for (Structure structure : structures) if (id.equals(structure.getId())) return structure;
        return null;
    }

    public static void addStructure(Structure structure) {
        structures.add(structure);
    }
}
