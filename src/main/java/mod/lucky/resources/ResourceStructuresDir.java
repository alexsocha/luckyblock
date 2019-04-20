package mod.lucky.resources;

import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public class ResourceStructuresDir extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
    }

    @Override
    public String getDirectory() {
        return "structures";
    }
}
