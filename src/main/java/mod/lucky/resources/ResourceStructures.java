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

                Lucky.addStructure(structure);
            }
        } catch (Exception e) {
            System.err.println("Lucky Block: Error reading 'structures.txt'");
            e.printStackTrace();
        }
    }

    @Override
    public String getPath() {
        return "structures.txt";
    }
}
