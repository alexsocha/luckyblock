package mod.lucky.init;

import mod.lucky.render.RenderLuckyPotion;
import mod.lucky.render.RenderLuckyProjectile;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import mod.lucky.Lucky;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourcePack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@OnlyIn(Dist.CLIENT)
public class SetupClient {

    private static void registerAddonResources() {
        try {
            Minecraft mc = Minecraft.getInstance();
            ResourceManager resourceLoader = new ResourceManager(mc.gameDir);
            for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
                IResourcePack pack = pluginLoader.getResourcePack();
                mc.getResourceManager().addResourcePack(pack);
            }

        } catch (Exception e) {
            Lucky.error(e, "Error registering add-on resources");
        }
    }

    private static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(
            EntityLuckyProjectile.class, RenderLuckyProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(
            EntityLuckyPotion.class, RenderLuckyPotion::new);
    }

    public static void setup() {
        registerEntityRenderers();
        registerAddonResources();
    }
}
