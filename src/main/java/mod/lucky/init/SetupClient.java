package mod.lucky.init;

import mod.lucky.render.RenderLuckyPotion;
import mod.lucky.render.RenderLuckyProjectile;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import mod.lucky.Lucky;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourcePack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import static mod.lucky.init.SetupCommon.ENTITY_LUCKY_POTION;
import static mod.lucky.init.SetupCommon.ENTITY_LUCKY_PROJECTILE;

@OnlyIn(Dist.CLIENT)
public class SetupClient {

    private static void registerAddonResources() {
        try {
            Minecraft mc = Minecraft.getInstance();
            ResourceManager resourceLoader = new ResourceManager(mc.gameDir);
            for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
                IResourcePack pack = pluginLoader.getResourcePack();
                IResourceManager resourceManager = mc.getResourceManager();
                if (resourceManager instanceof SimpleReloadableResourceManager) {
                    ((SimpleReloadableResourceManager) resourceManager).addResourcePack(pack);
                }
            }

        } catch (Exception e) {
            Lucky.error(e, "Error registering add-on resources");
        }
    }

    private static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(
            ENTITY_LUCKY_PROJECTILE, RenderLuckyProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(
            ENTITY_LUCKY_POTION, RenderLuckyPotion::new);
    }

    public static void setup() {
        registerEntityRenderers();
        registerAddonResources();
    }
}
