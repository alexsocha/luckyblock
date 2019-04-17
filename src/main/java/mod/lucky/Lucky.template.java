package mod.lucky;

import java.io.File;
import java.util.ArrayList;
import mod.lucky.block.BlockLuckyBlock;
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

@Mod(modid = Lucky.MODID, name = Lucky.NAME, version = Lucky.VERSION)
public class Lucky {
  @Instance(Lucky.MODID)
  private static Lucky instance;

  public static final String MODID = "${project.modid}";
  public static final String VERSION = "${project.version}";
  public static final String MC_VERSION = "${project.mcversion}";
  public static final String NAME = "Lucky Block";

  public static BlockLuckyBlock lucky_block;
  public static ItemLuckySword lucky_sword;
  public static ItemLuckyBow lucky_bow;
  public static ItemLuckyPotion lucky_potion;
  public static ArrayList<PluginLoader> lucky_block_plugins;
  public static ArrayList<Structure> structures;

  public static ResourceLoader resourceLoader;
  private LuckyTickHandler tickHandler;

  public static int ENTITY_ID = 24653;

  @SidedProxy(clientSide = "mod.lucky.client.ClientProxy", serverSide = "mod.lucky.CommonProxy")
  public static CommonProxy proxy;

  public static SimpleNetworkWrapper networkHandler;

  public Lucky() {
    networkHandler = NetworkRegistry.INSTANCE.newSimpleChannel("LuckyBlock");
    networkHandler.registerMessage(
        ParticlePacket.Handler.class, ParticlePacket.class, 0, Side.CLIENT);

    // Lucky Block
    lucky_block =
        (BlockLuckyBlock)
            new BlockLuckyBlock(Material.WOOD)
                .setUnlocalizedName("luckyBlock")
                .setHardness(0.2F)
                .setResistance(6000000.0F)
                .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
                .setRegistryName("lucky_block");
    lucky_block.setHarvestLevel("pickaxe", 0);
    // Lucky Sword
    lucky_sword =
        (ItemLuckySword)
            new ItemLuckySword()
                .setUnlocalizedName("luckySword")
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("lucky_sword");
    // Lucky Bow
    lucky_bow =
        (ItemLuckyBow)
            new ItemLuckyBow()
                .setUnlocalizedName("luckyBow")
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("lucky_bow");
    // Lucky Potion
    lucky_potion =
        (ItemLuckyPotion)
            new ItemLuckyPotion()
                .setUnlocalizedName("luckyPotion")
                .setCreativeTab(CreativeTabs.COMBAT)
                .setRegistryName("lucky_potion");

    lucky_block_plugins = new ArrayList<PluginLoader>();
    structures = new ArrayList<Structure>();

    DropFunction.registerFunctions();
    Lucky.resourceLoader = new ResourceLoader(new File("."));
    Lucky.resourceLoader.registerPlugins();
    Lucky.resourceLoader.extractDefaultResources();
    Lucky.resourceLoader.loadAllResources(false);
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
    GameRegistry.registerWorldGenerator(lucky_block.getWorldGenerator(), 1);

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
}
