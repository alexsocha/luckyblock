package mod.lucky.resources;

import mod.lucky.drop.DropContainer;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceSwordDrops extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            if (loader.getSword() == null) return;
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                DropContainer drop = new DropContainer();
                drop.readFromString(curLine);
                loader.getSword().getLuckyItem().getDropProcessor().registerDrop(drop);
            }
        } catch (Exception e) { this.logError(); }
    }

    @Override
    public String getPath() { return "sword_drops.txt"; }

    @Override
    public boolean isOptional() { return true; }
}
