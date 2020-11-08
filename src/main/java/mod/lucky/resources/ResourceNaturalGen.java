package mod.lucky.resources;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;
import mod.lucky.world.LuckyWorldFeature;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class ResourceNaturalGen extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            LuckyWorldFeature feature = new LuckyWorldFeature(NoFeatureConfig.field_236558_a_);
            feature.init(loader.getBlock());

            String section = "";
            String curLine;
            ResourceLocation dimension = new ResourceLocation("minecraft:overworld");
            while ((curLine = reader.readLine()) != null) {
                if (curLine.startsWith(">")) {
                    String dimensionName = curLine.substring(1).trim();
                    if (dimensionName.equals("surface")) dimension = new ResourceLocation("minecraft:overworld");
                    else if (dimensionName.equals("nether")) dimension = new ResourceLocation("minecraft:the_nether");
                    else if (dimensionName.equals("end")) dimension = new ResourceLocation("minecraft:the_end");
                    else dimension = new ResourceLocation(dimensionName);
                    continue;
                }

                DropFull drop = new DropFull();
                drop.readFromString(curLine);
                feature.addDrop(dimension, drop);
            }

            Lucky.worldFeatures.add(feature);
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() { return "natural_gen.txt"; }
}
