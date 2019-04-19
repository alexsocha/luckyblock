package mod.lucky.resources;

import mod.lucky.drop.DropContainer;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceDrops extends BaseResource {
  @Override
  public void process(LuckyReader reader, BaseLoader loader) {
    try {
      String curLine;
      while ((curLine = reader.readLine()) != null) {
        DropContainer drop = new DropContainer();
        drop.readFromString(curLine);
        loader.getBlock().getDropProcessor().registerDrop(drop);
      }
    } catch (Exception e) {
      System.err.println("Lucky Block: Error reading 'drops.txt'");
      e.printStackTrace();
    }
  }

  @Override
  public String getDirectory() {
    return "drops.txt";
  }
}
