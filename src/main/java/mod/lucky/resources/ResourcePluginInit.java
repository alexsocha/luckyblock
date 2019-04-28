package mod.lucky.resources;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.util.LuckyReader;

public class ResourcePluginInit extends BaseResource {
    /*
    private static String camelFromUnderscore(String string) {
        String upperCamel = Stream.of(string.split("_"))
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
            .collect(Collectors.joining());

        return upperCamel.substring(0, 1).toLowerCase() + upperCamel.substring(1);
    }
    */

    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            if (!(loader instanceof PluginLoader)) return;

            String blockId = "random_block";
            String swordId = null;
            String bowId = null;
            String potionId = null;
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                String name = curLine.substring(0, curLine.indexOf('='));
                String value = curLine.substring(curLine.indexOf('=') + 1);

                if (name.equalsIgnoreCase("id") || name.equalsIgnoreCase("block_id")) blockId = value;
                if (name.equalsIgnoreCase("sword_id")) swordId = value;
                if (name.equalsIgnoreCase("bow_id")) bowId = value;
                if (name.equalsIgnoreCase("potion_id")) potionId = value;
            }

            ((PluginLoader) loader).setPluginName(blockId);

            loader.setBlock((BlockLuckyBlock)
                new BlockLuckyBlock().setRegistryName(blockId));

            if (swordId != null) loader.setSword((ItemLuckySword)
                new ItemLuckySword().setRegistryName(swordId));

            if (bowId != null) loader.setBow((ItemLuckyBow)
                new ItemLuckyBow().setRegistryName(bowId));

            if (potionId != null) loader.setPotion((ItemLuckyPotion)
                new ItemLuckyPotion().setRegistryName(potionId));

            Lucky.luckyBlockPlugins.add((PluginLoader) loader);
        } catch (Exception e) { this.logError(); }
    }

    @Override
    public String getPath() { return "plugin_init.txt"; }
}
