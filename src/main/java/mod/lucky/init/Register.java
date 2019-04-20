package mod.lucky.init;

import mod.lucky.Lucky;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.resources.loader.PluginLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Register {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
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
    
    private static void registerLuckCraftingItem(IForgeRegistry<IRecipe> registry,

                                                 @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event
            .getRegistry()
            .register(
                Lucky.luckyBlock
                    .getBlockRecipe()
                    .setRegistryName(Lucky.luckyBlock.getRegistryName().toString()));
        event
            .getRegistry()
            .register(
                Lucky.luckyBlock
                    .getCrafting()
                    .setRegistryName(Lucky.luckyBlock.getRegistryName().toString() + "_luck"));
        event
            .getRegistry()
            .register(
                Lucky.luckySword
                    .getCrafting()
                    .setRegistryName(Lucky.luckySword.getRegistryName().toString() + "_luck"));
        event
            .getRegistry()
            .register(
                Lucky.luckyBow
                    .getCrafting()
                    .setRegistryName(Lucky.luckyBow.getRegistryName().toString() + "_luck"));
        event
            .getRegistry()
            .register(
                Lucky.luckyPotion
                    .getCrafting()
                    .setRegistryName(Lucky.luckyPotion.getRegistryName().toString() + "_luck"));

        for (PluginLoader plugin : Lucky.luckyBlockPlugins) {
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
