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

public class ClientProxy extends CommonProxy {
  public ClientProxy() {
    preRegister();
  }

  @Override
  public void preRegister() {
    try {
      RenderingRegistry.registerEntityRenderingHandler(
          EntityLuckyProjectile.class, new RenderFactoryLuckyProjectile());
      RenderingRegistry.registerEntityRenderingHandler(
          EntityLuckyPotion.class, new RenderFactoryLuckyPotion());

      ResourceLoader resourceLoader = new ResourceLoader(Minecraft.getMinecraft().mcDataDir);
      List defaultResourcePacks =
          ObfuscationReflectionHelper.getPrivateValue(
              FMLClientHandler.class, FMLClientHandler.instance(), "resourcePackList");

      for (PluginLoader pluginLoader : resourceLoader.getPluginLoaders()) {
        System.out.println(pluginLoader.getPluginFile().getAbsolutePath());
        defaultResourcePacks.add(pluginLoader.getResourcePack());
      }

    } catch (Exception e) {
      System.err.println("Lucky Block: Error loading client plugins");
      e.printStackTrace();
    }
  }

  @Override
  public void register() {
    Minecraft.getMinecraft()
        .getRenderItem()
        .getItemModelMesher()
        .register(
            Item.getItemFromBlock(Lucky.lucky_block),
            0,
            new ModelResourceLocation("lucky:lucky_block", "inventory"));
    Minecraft.getMinecraft()
        .getRenderItem()
        .getItemModelMesher()
        .register(
            Lucky.lucky_sword, 0, new ModelResourceLocation("lucky:lucky_sword", "inventory"));
    Minecraft.getMinecraft()
        .getRenderItem()
        .getItemModelMesher()
        .register(
            Lucky.lucky_potion, 0, new ModelResourceLocation("lucky:lucky_potion", "inventory"));
    Minecraft.getMinecraft()
        .getRenderItem()
        .getItemModelMesher()
        .register(Lucky.lucky_bow, 0, new ModelResourceLocation("lucky:lucky_bow", "inventory"));

    MinecraftForge.EVENT_BUS.register(new LuckyClientEventHandler());

    for (PluginLoader pluginLoader : Lucky.lucky_block_plugins) {
      Minecraft.getMinecraft()
          .getRenderItem()
          .getItemModelMesher()
          .register(
              Item.getItemFromBlock(pluginLoader.getBlock()),
              0,
              new ModelResourceLocation(
                  Block.REGISTRY.getNameForObject(pluginLoader.getBlock()).toString(),
                  "inventory"));
      if (pluginLoader.getSword() != null)
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(
                pluginLoader.getSword(),
                0,
                new ModelResourceLocation(
                    Item.REGISTRY.getNameForObject(pluginLoader.getSword()).toString(),
                    "inventory"));
      if (pluginLoader.getPotion() != null)
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(
                pluginLoader.getPotion(),
                0,
                new ModelResourceLocation(
                    Item.REGISTRY.getNameForObject(pluginLoader.getPotion()).toString(),
                    "inventory"));
      if (pluginLoader.getBow() != null)
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(
                pluginLoader.getBow(),
                0,
                new ModelResourceLocation(
                    Item.REGISTRY.getNameForObject(pluginLoader.getBow()).toString(), "inventory"));
    }
  }
}
