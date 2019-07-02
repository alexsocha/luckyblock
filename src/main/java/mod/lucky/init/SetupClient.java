package mod.lucky.init;

import java.util.List;

import mod.lucky.Lucky;
import mod.lucky.client.RenderLuckyProjectile;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSprite;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.packs.ResourcePackLoader;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class SetupClient {

    public static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(
            EntityLuckyProjectile.class, RenderLuckyProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(
            EntityLuckyPotion.class, manager ->
                new RenderSprite<EntityLuckyPotion>(manager, Lucky.luckyPotion,
                    Minecraft.getInstance().getItemRenderer()));

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
            Lucky.error(e, "Error registering add-on resources");
        }
    }

    private static void registerItemModel(Item item) {
        /*
        Minecraft.getInstance()
            .getItemRenderer()
            .getItemModelMesher()
            .register(item, new ModelResourceLocation(item.getRegistryName().toString()));
         */
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

    /*
    @SubscribeEvent
    public static void registerEntities(FMLLoadCompleteEvent event) {
        registerEntityRenderers();
    }
     */

    public static void setup() {
        registerEntityRenderers();
        //registerEntityRenderers();
        //MinecraftForge.EVENT_BUS.register(Lucky.tickHandler);
        /*
        registerPluginResources();
        registerAllItemModels();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
         */
    }
}
