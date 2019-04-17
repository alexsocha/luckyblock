package mod.lucky.resources;

import mod.lucky.drop.DropContainer;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceSwordDrops extends BaseResource {
  @Override
  public void process(LuckyReader reader, BaseLoader loader) {
    try {
      String curLine;
      while ((curLine = reader.readLine()) != null) {
        DropContainer drop = new DropContainer();
        drop.readFromString(curLine);
        loader.getSword().getDropProcessor().registerDrop(drop);
      }
    } catch (Exception e) {
      System.err.println("Lucky Block: Error reading 'sword_drops.txt'");
      e.printStackTrace();
    }
  }

  @Override
  public String getDirectory() {
    return "sword_drops.txt";
  }

  @Override
  public boolean isOptional() {
    return true;
  }
}
