package mod.lucky.resources;

import mod.lucky.drop.DropFull;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceBowDrops extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            if (loader.getBow() == null) return;
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                DropFull drop = new DropFull();
                drop.readFromString(curLine);
                loader.getBow().getLuckyItem().getDropProcessor().registerDrop(drop);
            }
        } catch (Exception e) { this.logError(); }
    }

    @Override
    public String getPath() { return "bow_drops.txt"; }

    @Override
    public boolean isOptional() { return true; }
}
