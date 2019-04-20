package mod.lucky.resources;

import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceStructureFile extends BaseResource {
    private String name;

    public ResourceStructureFile(String name) {
        this.name = name;
    }

    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
    }

    @Override
    public String getDirectory() {
        return "structures/" + this.name;
    }
}
