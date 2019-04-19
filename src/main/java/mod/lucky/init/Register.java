package mod.lucky.init;

import mod.lucky.Lucky;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.resources.loader.PluginLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Register {
  @SubscribeEvent
  public static void registerBlocks(RegistryEvent.Register<Block> event) {
    event.getRegistry().register(Lucky.lucky_block);
    for (PluginLoader plugin : Lucky.lucky_block_plugins)
      event.getRegistry().register(plugin.getBlock());
  }

  @SubscribeEvent
  public static void registerItems(RegistryEvent.Register<Item> event) {
    event
        .getRegistry()
        .register(
            new ItemLuckyBlock(Lucky.lucky_block)
                .setRegistryName(Lucky.lucky_block.getRegistryName()));
    event.getRegistry().register(Lucky.lucky_sword);
    event.getRegistry().register(Lucky.lucky_bow);
    event.getRegistry().register(Lucky.lucky_potion);

    for (PluginLoader plugin : Lucky.lucky_block_plugins) {
      event
          .getRegistry()
          .register(
              new ItemLuckyBlock(plugin.getBlock())
                  .setRegistryName(plugin.getBlock().getRegistryName()));
      if (plugin.getSword() != null) event.getRegistry().register(plugin.getSword());
      if (plugin.getBow() != null) event.getRegistry().register(plugin.getBow());
      if (plugin.getPotion() != null) event.getRegistry().register(plugin.getPotion());
    }

    Lucky.resourceLoader.loadAllResources(true);
  }

  @SubscribeEvent
  public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    event
        .getRegistry()
        .register(
            Lucky.lucky_block
                .getBlockRecipe()
                .setRegistryName(Lucky.lucky_block.getRegistryName().toString()));
    event
        .getRegistry()
        .register(
            Lucky.lucky_block
                .getCrafting()
                .setRegistryName(Lucky.lucky_block.getRegistryName().toString() + "_luck"));
    event
        .getRegistry()
        .register(
            Lucky.lucky_sword
                .getCrafting()
                .setRegistryName(Lucky.lucky_sword.getRegistryName().toString() + "_luck"));
    event
        .getRegistry()
        .register(
            Lucky.lucky_bow
                .getCrafting()
                .setRegistryName(Lucky.lucky_bow.getRegistryName().toString() + "_luck"));
    event
        .getRegistry()
        .register(
            Lucky.lucky_potion
                .getCrafting()
                .setRegistryName(Lucky.lucky_potion.getRegistryName().toString() + "_luck"));

    for (PluginLoader plugin : Lucky.lucky_block_plugins) {
      if (plugin.getBlock().getBlockRecipe() != null)
        event
            .getRegistry()
            .register(
                plugin
                    .getBlock()
                    .getBlockRecipe()
                    .setRegistryName(plugin.getBlock().getRegistryName().toString()));
      event
          .getRegistry()
          .register(
              plugin
                  .getBlock()
                  .getCrafting()
                  .setRegistryName(plugin.getBlock().getRegistryName().toString() + "_luck"));
      if (plugin.getSword() != null)
        event
            .getRegistry()
            .register(
                plugin
                    .getSword()
                    .getCrafting()
                    .setRegistryName(plugin.getSword().getRegistryName().toString() + "_luck"));
      if (plugin.getBow() != null)
        event
            .getRegistry()
            .register(
                plugin
                    .getBow()
                    .getCrafting()
                    .setRegistryName(plugin.getBow().getRegistryName().toString() + "_luck"));
      if (plugin.getPotion() != null)
        event
            .getRegistry()
            .register(
                plugin
                    .getPotion()
                    .getCrafting()
                    .setRegistryName(plugin.getPotion().getRegistryName().toString() + "_luck"));
    }
  }
}
