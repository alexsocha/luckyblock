package mod.lucky.init;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import mod.lucky.Lucky;
import mod.lucky.client.RenderLuckyProjectile;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSprite;
import net.minecraft.client.resources.ResourcePackInfoClient;
import net.minecraft.item.Item;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.TextComponentString;
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

            ResourcePackList defaultResourcePacks = Minecraft.getInstance().getResourcePackList();

            for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
                IResourcePack pack = pluginLoader.getResourcePack();
                String name = "mod:lucky:" + pluginLoader.getPluginName();

                IPackFinder packFinder = new IPackFinder() {
                    @Override
                    public <T extends ResourcePackInfo> void addPackInfosToMap(
                        Map<String, T> nameToPackMap,
                        ResourcePackInfo.IFactory<T> packInfoFactory) {

                        ResourcePackInfoClient packInfo = new ResourcePackInfoClient(
                            name,
                            true, // enabled by default?
                            () -> pack,
                            pack,
                            new PackMetadataSection(
                                new TextComponentString(pluginLoader.getPluginName()), 1),
                            ResourcePackInfo.Priority.TOP,
                            true);

                        try { nameToPackMap.put(name, (T) packInfo); }
                        catch (Exception e) {
                            Lucky.error(e, "Error loading resource pack for add-on: "
                                + pluginLoader.getPluginName());
                        }

                    }
                };

                ResourcePackList<ResourcePackInfo> resourcePacks
                    = new ResourcePackList<>(ResourcePackInfo::new);
                resourcePacks.addPackFinder(packFinder);

                //ResourcePackLoader.loadResourcePacks(resourcePacks);
                defaultResourcePacks.addPackFinder(packFinder);
                defaultResourcePacks.reloadPacksFromFinders();
            }

        } catch (Exception e) {
            Lucky.error(e, "Error registering add-on resources");
        }
    }

    public static void setup() {
        registerEntityRenderers();
        registerPluginResources();
    }
}
