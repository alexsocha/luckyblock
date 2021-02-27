package mod.lucky.resources;

import mod.lucky.Lucky;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.structure.Structure;
import mod.lucky.util.LuckyReader;

public class ResourceStructures extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                Structure properties = new Structure();
                properties.readProperties(curLine, loader);

                Structure structure = properties.newTypeInstance();
                structure.readFromFile();

                Lucky.structures.add(structure);
            }
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() {
        return "structures.txt";
    }
}
