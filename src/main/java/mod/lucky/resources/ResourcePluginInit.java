package mod.lucky.resources;

import java.util.Locale;
import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.util.LuckyReader;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import org.apache.commons.lang3.text.WordUtils;

public class ResourcePluginInit extends BaseResource {
  @Override
  public void process(LuckyReader reader, BaseLoader loader) {
    PluginLoader pluginLoader = (PluginLoader) loader;
    try {
      String blockId = "random_block";
      String swordId = null;
      String bowId = null;
      String potionId = null;
      String curLine;
      while ((curLine = reader.readLine()) != null) {
        String name = curLine.substring(0, curLine.indexOf('='));
        String value = curLine.substring(curLine.indexOf('=') + 1, curLine.length());

        if (name.equalsIgnoreCase("id") || name.equalsIgnoreCase("block_id")) blockId = value;
        if (name.equalsIgnoreCase("sword_id")) swordId = value;
        if (name.equalsIgnoreCase("bow_id")) bowId = value;
        if (name.equalsIgnoreCase("potion_id")) potionId = value;
      }

      String camelIdBlock =
          WordUtils.capitalizeFully(blockId, new char[] {'_'}).replaceAll("_", "");
      camelIdBlock =
          String.valueOf(camelIdBlock.charAt(0)).toLowerCase(Locale.ENGLISH)
              + camelIdBlock.substring(1, camelIdBlock.length());

      BlockLuckyBlock lucky_block =
          (BlockLuckyBlock)
              new BlockLuckyBlock(Material.WOOD)
                  .setUnlocalizedName(camelIdBlock)
                  .setHardness(0.2F)
                  .setResistance(6000000.0F)
                  .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
                  .setRegistryName(blockId);
      lucky_block.setHarvestLevel("pickaxe", 0);

      pluginLoader.setPluginName(blockId);

      ItemLuckySword lucky_sword = null;
      if (swordId != null) {
        String camelId = WordUtils.capitalizeFully(swordId, new char[] {'_'}).replaceAll("_", "");
        camelId =
            String.valueOf(camelId.charAt(0)).toLowerCase(Locale.ENGLISH)
                + camelId.substring(1, camelId.length());
        lucky_sword =
            (ItemLuckySword)
                new ItemLuckySword()
                    .setUnlocalizedName(camelId)
                    .setCreativeTab(CreativeTabs.COMBAT)
                    .setRegistryName(swordId);
      }

      ItemLuckyBow lucky_bow = null;
      if (bowId != null) {
        String camelId = WordUtils.capitalizeFully(bowId, new char[] {'_'}).replaceAll("_", "");
        camelId =
            String.valueOf(camelId.charAt(0)).toLowerCase(Locale.ENGLISH)
                + camelId.substring(1, camelId.length());
        lucky_bow =
            (ItemLuckyBow)
                new ItemLuckyBow()
                    .setUnlocalizedName(camelId)
                    .setCreativeTab(CreativeTabs.COMBAT)
                    .setRegistryName(bowId);
        lucky_bow.setBowTextureName("lucky:" + bowId);
      }

      ItemLuckyPotion lucky_potion = null;
      if (potionId != null) {
        String camelId = WordUtils.capitalizeFully(potionId, new char[] {'_'}).replaceAll("_", "");
        camelId =
            String.valueOf(camelId.charAt(0)).toLowerCase(Locale.ENGLISH)
                + camelId.substring(1, camelId.length());
        lucky_potion =
            (ItemLuckyPotion)
                new ItemLuckyPotion()
                    .setUnlocalizedName(camelId)
                    .setCreativeTab(CreativeTabs.MISC)
                    .setRegistryName(potionId);
      }

      loader.setLuckyBlockItems(lucky_block, lucky_sword, lucky_bow, lucky_potion);
      Lucky.luckyBlockPlugins.add(pluginLoader);
    } catch (Exception e) {
      System.err.println(
          "Lucky Block: Error reading 'plugin_init.txt' from plugin '"
              + pluginLoader.getPluginFile()
              + "'");
      e.printStackTrace();
    }
  }

  @Override
  public String getDirectory() {
    return "plugin_init.txt";
  }
}
