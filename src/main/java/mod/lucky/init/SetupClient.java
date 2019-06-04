package mod.lucky.init;

import java.util.List;

import mod.lucky.Lucky;
import mod.lucky.client.ClientEventHandler;
import mod.lucky.client.RenderLuckyPotion;
import mod.lucky.client.RenderLuckyProjectile;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.packs.ResourcePackLoader;

@OnlyIn(Dist.CLIENT)
public class SetupClient {
    private static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityLuckyProjectile.class,
            new IRenderFactory<EntityLuckyProjectile>() {
                @Override
                public Render<EntityLuckyProjectile> createRenderFor(RenderManager manager) {
                    return new RenderLuckyProjectile(manager);
                }
            });
        RenderingRegistry.registerEntityRenderingHandler(EntityLuckyPotion.class,
            new IRenderFactory<EntityLuckyPotion>() {
                @Override
                public Render<EntityLuckyPotion> createRenderFor(RenderManager manager) {
                    return new RenderLuckyPotion(manager,
                        Minecraft.getInstance().getItemRenderer());
                }
            });
    }

    private static void registerPluginResources() {
        try {
            ResourceManager resourceLoader = new ResourceManager(Minecraft.getInstance().gameDir);
            List defaultResourcePacks =
                ObfuscationReflectionHelper.getPrivateValue(
                    ResourcePackLoader.class, new ResourcePackLoader(), "resourcePackList");

            for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
                defaultResourcePacks.add(pluginLoader.getResourcePack());
            }

        } catch (Exception e) {
            Lucky.LOGGER.error("Error registering add-on resources");
            e.printStackTrace();
        }
    }

    private static void registerItemModel(Item item) {
        Minecraft.getInstance()
            .getItemRenderer()
            .getItemModelMesher()
            .register(item, new ModelResourceLocation(item.getRegistryName().toString()));
    }

    public static void registerAllItemModels() {
        registerItemModel(Item.BLOCK_TO_ITEM.get(Lucky.luckyBlock));
        registerItemModel(Lucky.luckySword);
        registerItemModel(Lucky.luckyPotion);
        registerItemModel(Lucky.luckyBow);

        for (PluginLoader loader : Lucky.luckyBlockPlugins) {
            registerItemModel(Item.BLOCK_TO_ITEM.get(loader.getBlock()));
            if (loader.getSword() != null) registerItemModel(loader.getSword());
            if (loader.getPotion() != null) registerItemModel(loader.getPotion());
            if (loader.getBow() != null) registerItemModel(loader.getBow());
        }
    }

    public static void setup() {
        registerEntityRenderers();
        registerPluginResources();
        registerAllItemModels();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new LuckyTickHandler());

    }
}
