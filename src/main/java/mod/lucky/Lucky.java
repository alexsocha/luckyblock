package mod.lucky;

import java.io.File;
import java.util.ArrayList;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.client.SetupClient;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.network.ParticlePacket;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceLoader;
import mod.lucky.structure.Structure;
import mod.lucky.structure.rotation.Rotations;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

// ======= 1.13 ===========
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.versions.MCPVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("lucky")
public class Lucky {
  public static String version;
  public static String mcversion = MCPVersion.getMCVersion();

  public static BlockLuckyBlock luckyBlock;
  public static ItemLuckySword luckySword;
  public static ItemLuckyBow luckyBow;
  public static ItemLuckyPotion luckyPotion;
  public static ArrayList<PluginLoader> luckyBlockPlugins;
  public static ArrayList<Structure> structures;

  public static ResourceLoader resourceLoader;
  private LuckyTickHandler tickHandler;

  public static int ENTITY_ID = 24653;

  @SidedProxy(clientSide = "mod.lucky.client.ClientProxy", serverSide = "mod.lucky.CommonProxy")
  public static CommonProxy proxy;

  public static SimpleNetworkWrapper networkHandler;

  private static final Logger LOGGER = LogManager.getLogger();


  public Lucky() {
    // ========== begin 1.13 ==========
    Lucky.verison = ModLoadingContext.getActiveContainer().getModInfo().getVersion().toString();
    LOGGER.info("==========" + Lucky.mcversion + ", " + Lucky.version);

    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

    MinecraftForge.EVENT_BUS.register(this);
    // ======== end 1.13 ========

  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.register();

    // Lucky Projectile
    EntityRegistry.registerModEntity(
        new ResourceLocation("lucky", "LuckyProjectile"),
        EntityLuckyProjectile.class,
        "LuckyProjectile",
        Lucky.ENTITY_ID++,
        this,
        128,
        20,
        true);
    // Lucky Potion Entity
    EntityRegistry.registerModEntity(
        new ResourceLocation("lucky", "LuckyPotion"),
        EntityLuckyPotion.class,
        "LuckyPotion",
        Lucky.ENTITY_ID++,
        this,
        128,
        20,
        true);

    GameRegistry.registerTileEntity(TileEntityLuckyBlock.class, "luckyBlockData");
    GameRegistry.registerWorldGenerator(luckyBlock.getWorldGenerator(), 1);

    this.tickHandler = new LuckyTickHandler();
    MinecraftForge.EVENT_BUS.register(this.tickHandler);

    Rotations.registerRotationHandlers();
  }

  public static Lucky getInstance() {
    return instance;
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {}

  public ResourceLoader getResourceLoader() {
    return this.resourceLoader;
  }

  public LuckyTickHandler getTickHandler() {
    return this.tickHandler;
  }

  public static ArrayList<Structure> getStructures() {
    return structures;
  }

  public static Structure getStructure(String id) {
    for (Structure structure : structures) if (id.equals(structure.getId())) return structure;
    return null;
  }

  public static void addStructure(Structure structure) {
    structures.add(structure);
  }

  // =============== 1.13 =======================
  private void setup(final FMLCommonSetupEvent event) {
    networkHandler = NetworkRegistry.INSTANCE.newSimpleChannel("LuckyBlock");
    networkHandler.registerMessage(
        ParticlePacket.Handler.class, ParticlePacket.class, 0, Side.CLIENT);

    // Lucky Block
    luckyBlock = new BlockLuckyBlock(Material.WOOD)
                .setHardness(0.2F)
                .setResistance(6000000.0F)
                .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
                .setRegistryName("luckyBlock");
    luckyBlock.setHarvestLevel("pickaxe", 0);
    // Lucky Sword
    luckySword = new ItemLuckySword()
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("luckySword");
    // Lucky Bow
    luckyBow = new ItemLuckyBow()
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("luckyBow");
    // Lucky Potion
    luckyPotion = new ItemLuckyPotion()
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("luckyPotion");

    luckyBlockPlugins = new ArrayList<PluginLoader>();
    structures = new ArrayList<Structure>();

    DropFunction.registerFunctions();
    Lucky.resourceLoader = new ResourceLoader(new File("."));
    Lucky.resourceLoader.registerPlugins();
    Lucky.resourceLoader.extractDefaultResources();
    Lucky.resourceLoader.loadAllResources(false);
    // some preinit code
    LOGGER.info("SETUP");
  }

  private void setupClient(final FMLClientSetupEvent event) {
    SetupClient.setup();
  }

  @SubscribeEvent
  public void onServerStarting(FMLServerStartingEvent event) {
    LOGGER.info("HELLO from server starting");
  }

  @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
  public static class RegistryEvents {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
      // register a new block here
      LOGGER.info("HELLO from Register Block");
    }
  }
}
