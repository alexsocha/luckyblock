package mod.lucky.init;

import javafx.geometry.Side;
import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.network.ParticlePacket;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceRegistry;
import mod.lucky.structure.rotation.Rotations;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.ArrayList;

public class SetupCommon {
    private static void setupNetwork() {
        // network channel
        Lucky.networkChannel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation("lucky", "lucky_channel"))
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION)
            .simpleChannel();

        // packet for spawning particles
        Lucky.networkChannel.messageBuilder(ParticlePacket.class, 0)
            .decoder(ParticlePacket::decode)
            .encoder(ParticlePacket::encode)
            .consumer(ParticlePacket::handle)
            .add();
    }

    public static void setupEntities() {
        // lucky projectile entity
        ForgeRegistries.ENTITIES.register(
            EntityType.register("lucky_projectile",
                EntityType.Builder.create(
                    EntityLuckyProjectile.class, EntityLuckyProjectile::new)));

        // lucky potion entity
        ForgeRegistries.ENTITIES.register(
            EntityType.register("lucky_potion",
                EntityType.Builder.create(
                    EntityLuckyPotion.class, EntityLuckyPotion::new)));

        // lucky block tile entity
        ForgeRegistries.TILE_ENTITIES.register(
            TileEntityType.register("lucky_block",
                TileEntityType.Builder.create(TileEntityLuckyBlock::new)));
    }

    public static void setupItemsAndBlocks() {
        // lucky block
        Lucky.luckyBlock = new BlockLuckyBlock(Material.WOOD)
            .setHardness(0.2F)
            .setResistance(6000000.0F)
            .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
            .setRegistryName("lucky_block");
        Lucky.luckyBlock.setHarvestLevel("pickaxe", 0);
        // lucky sword
        Lucky.luckySword = (ItemLuckySword) new ItemLuckySword()
            .setRegistryName("lucky_sword");
        // lucky bow
        Lucky.luckyBow = (ItemLuckyBow) new ItemLuckyBow()
            .setRegistryName("lucky_bow");
        // lucky potion
        Lucky.luckyPotion = (ItemLuckyPotion) new ItemLuckyPotion()
            .setRegistryName("lucky_potion");
    }

    public static void setup() {
        SetupCommon.setupNetwork();
        SetupCommon.setupItemsAndBlocks();
        SetupCommon.setupEntities();

        Lucky.luckyBlockPlugins = new ArrayList<PluginLoader>();
        //Lucky.structures = new ArrayList<Structure>();

        // lucky block worl generator
        //GameRegistry.registerWorldGenerator(luckyBlock.getWorldGenerator(), 1);

        Lucky.tickHandler = new LuckyTickHandler();
        MinecraftForge.EVENT_BUS.register(Lucky.tickHandler);

        Rotations.registerRotationHandlers();
        DropFunction.registerFunctions();

        Lucky.resourceRegistry = new ResourceRegistry(new File("."));
        Lucky.resourceRegistry.registerPlugins();
        Lucky.resourceRegistry.extractDefaultResources();
        Lucky.resourceRegistry.loadAllResources(false);
    }
}
