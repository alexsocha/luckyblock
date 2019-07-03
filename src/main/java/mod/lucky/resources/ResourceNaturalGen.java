package mod.lucky.resources;

import mod.lucky.drop.DropFull;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;
import mod.lucky.world.LuckyGenerator;

public class ResourceNaturalGen extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            LuckyGenerator generator = LuckyGenerator.registerNew(loader.getBlock());
            loader.getBlock().setWorldGenerator(generator);

            String section = "";
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                if (curLine.startsWith(">")) {
                    section = curLine;
                    continue;
                }

                DropFull drop = new DropFull();
                drop.readFromString(curLine);
                if (section.equals(">surface")) loader.getBlock().getWorldGenerator().addSurfacedDrop(drop);
                if (section.equals(">nether")) loader.getBlock().getWorldGenerator().addNetherDrop(drop);
                if (section.equals(">end")) loader.getBlock().getWorldGenerator().addEndDrop(drop);
            }
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() { return "natural_gen.txt"; }
}
