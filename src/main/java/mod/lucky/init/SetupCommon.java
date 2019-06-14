package mod.lucky.init;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.crafting.RecipeLuckCrafting;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.ArrayList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupCommon {
    public static final TileEntityType<TileEntityLuckyBlock> LUCKY_BLOCK_TE_TYPE =
        TileEntityType.register("lucky:lucky_block",
            TileEntityType.Builder.create(TileEntityLuckyBlock::new));

    public static final RecipeSerializers.
        SimpleSerializer<RecipeLuckCrafting> LUCK_CRAFTING_SERIALIZER =
            RecipeSerializers.register(new RecipeSerializers.SimpleSerializer<>(
                "lucky:luck_crafting", RecipeLuckCrafting::new));

    public static final EntityType<EntityLuckyPotion> LUCKY_POTION_TYPE =
        EntityType.register("lucky:potion",
            EntityType.Builder.create(
                EntityLuckyPotion.class, EntityLuckyPotion::new));

    public static final EntityType<EntityLuckyProjectile> LUCKY_PROJECTILE_TYPE =
        EntityType.register("lucky:projectile",
            EntityType.Builder.create(
                EntityLuckyProjectile.class, EntityLuckyProjectile::new));

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        Lucky.LOGGER.info("registerBlocks ==================");

        event.getRegistry().register(Lucky.luckyBlock);
        for (PluginLoader plugin : Lucky.luckyBlockPlugins)
            event.getRegistry().register(plugin.getBlock());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(
            new ItemLuckyBlock(Lucky.luckyBlock)
                .setRegistryName(Lucky.luckyBlock.getRegistryName()));
        event.getRegistry().register(Lucky.luckySword);
        event.getRegistry().register(Lucky.luckyBow);
        event.getRegistry().register(Lucky.luckyPotion);

        for (PluginLoader plugin : Lucky.luckyBlockPlugins) {
            event.getRegistry().register(
                new ItemLuckyBlock(plugin.getBlock())
                    .setRegistryName(plugin.getBlock().getRegistryName()));

            if (plugin.getSword() != null) event.getRegistry().register(plugin.getSword());
            if (plugin.getBow() != null) event.getRegistry().register(plugin.getBow());
            if (plugin.getPotion() != null) event.getRegistry().register(plugin.getPotion());
        }

        Lucky.resourceRegistry.loadAllResources(true);
    }

    public static void setupEntities() {
        ForgeRegistries.ENTITIES.register(LUCKY_POTION_TYPE);
        ForgeRegistries.ENTITIES.register(LUCKY_PROJECTILE_TYPE);

        ForgeRegistries.TILE_ENTITIES.register(
            TileEntityType.register("lucky:lucky_block",
                TileEntityType.Builder.create(TileEntityLuckyBlock::new)));
    }

    public static void setupStatic() {
        Lucky.luckyBlock = (BlockLuckyBlock) new BlockLuckyBlock()
            .setRegistryName("lucky_block");
        Lucky.luckySword = (ItemLuckySword) new ItemLuckySword()
            .setRegistryName("lucky_sword");
        Lucky.luckyBow = (ItemLuckyBow) new ItemLuckyBow()
            .setRegistryName("lucky_bow");
        Lucky.luckyPotion = (ItemLuckyPotion) new ItemLuckyPotion()
            .setRegistryName("lucky_potion");

        //SetupCommon.setupEntities();

        Lucky.luckyBlockPlugins = new ArrayList<PluginLoader>();

        Lucky.resourceRegistry = new ResourceManager(new File("."));
        Lucky.tickHandler = new LuckyTickHandler();
        //MinecraftForge.EVENT_BUS.register(Lucky.tickHandler);

        DropFunction.registerFunctions();

        //Lucky.resourceRegistry.registerPlugins();
        //Lucky.resourceRegistry.extractDefaultResources();
        //Lucky.resourceRegistry.loadAllResources(false);
    }

    public static void setup() {
        Lucky.LOGGER.info("setup ==================");
    }
}
