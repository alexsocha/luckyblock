package mod.lucky.resources;

import mod.lucky.drop.DropFull;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourcePotionDrops extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            if (loader.getPotion() == null) return;
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                DropFull drop = new DropFull();
                drop.readFromString(curLine);
                loader.getPotion().getLuckyItem().getDropProcessor().registerDrop(drop);
            }
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() { return "potion_drops.txt"; }

    @Override
    public boolean isOptional() { return true; }
}
