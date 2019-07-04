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
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SetupClient {

    public static void registerAddonResources() {
        try {
            Minecraft mc = Minecraft.getInstance();
            ResourceManager resourceLoader = new ResourceManager(mc.gameDir);

            ResourcePackList packList = null;
            if (mc.getIntegratedServer() != null) {
                packList = mc.getIntegratedServer().getResourcePacks();
            }
            else
                packList = mc.getResourcePackList();

            for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
                IResourcePack pack = pluginLoader.getResourcePack();
                String name = "mod:lucky:" + pluginLoader.getPluginName();

                IPackFinder packFinder = new IPackFinder() {
                    @Override
                    public <T extends ResourcePackInfo> void addPackInfosToMap(
                        Map<String, T> nameToPackMap,
                        ResourcePackInfo.IFactory<T> packInfoFactory) {

                        T packInfo = packInfoFactory.create(
                            name,
                            true, // enabled by default
                            () -> pack,
                            pack,
                            new PackMetadataSection(
                                new TextComponentString(pluginLoader.getPluginName()), 4),
                            ResourcePackInfo.Priority.BOTTOM);

                        ObfuscationReflectionHelper.setPrivateValue(
                            ResourcePackInfo.class, packInfo, true, "hidden");

                        try { nameToPackMap.put(name, packInfo); }
                        catch (Exception e) {
                            Lucky.error(e, "Error loading resource pack for add-on: "
                                + pluginLoader.getPluginName());
                        }
                    }
                };
                packList.addPackFinder(packFinder);
            }
            packList.reloadPacksFromFinders();

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
