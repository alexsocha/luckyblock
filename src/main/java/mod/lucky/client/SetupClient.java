package mod.lucky.client;

import java.util.List;
import mod.lucky.CommonProxy;
import mod.lucky.Lucky;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceLoader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SetupClient {
  private static void registerEntities() {
      RenderingRegistry.registerEntityRenderingHandler(
          EntityLuckyProjectile.class, new RenderFactoryLuckyProjectile());
      RenderingRegistry.registerEntityRenderingHandler(
          EntityLuckyPotion.class, new RenderFactoryLuckyPotion());
  }

  private static void registerPluginResources() {
    try {
      ResourceLoader resourceLoader = new ResourceLoader(Minecraft.getInstance().mcDataDir);
      List defaultResourcePacks =
          ObfuscationReflectionHelper.getPrivateValue(
              FMLClientHandler.class, FMLClientHandler.instance(), "resourcePackList");

      for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
        defaultResourcePacks.add(pluginLoader.getResourcePack());
      }

    } catch (Exception e) {
      Lucky.LOGGER.error("Error registering add-on resources");
      e.printStackTrace();
    }
  }

  private static void registerItemModel(Item item, String resourceName) {
    Minecraft.getInstance()
        .getItemRenderer()
        .getItemModelMesher()
        .register(item, new ModelResourceLocation(item, "inventory"));
  }

  public static void registerAllItemModels() {
    registerItemModel(Lucky.luckyBlock, "lucky:luckyBlock");
    registerItemModel(Lucky.luckySword, "lucky:luckySword");
    registerItemModel(Lucky.luckyPotion, "lucky:luckyPotion");
    registerItemModel(Lucky.luckyBow, "lucky:luckyBow");

    MinecraftForge.EVENT_BUS.register(new LuckyClientEventHandler());

    for (PluginLoader loader : Lucky.luckyBlockPlugins) {
      registerItemModel(loader.getBlock(),
          Block.REGISTRY.getNameForObject(loader.getBlock()).toString());

      if (pluginLoader.getSword() != null)
        registerItemModel(loader.getSword(),
            Item.REGISTRY.getNameForObject(pluginLoader.getSword()).toString());

      if (pluginLoader.getPotion() != null)
        registerItemModel(loader.getPotion(),
            Item.REGISTRY.getNameForObject(pluginLoader.getPotion()).toString());

      if (pluginLoader.getBow() != null)
        registerItemModel(loader.getBow(),
            Item.REGISTRY.getNameForObject(pluginLoader.getBow()).toString());
    }
  }

  public static void setup() {
    registerEntities();
    registerPluginResources();
    registerAllItemModels();
  }
}
