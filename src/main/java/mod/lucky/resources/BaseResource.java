package mod.lucky.resources;

import mod.lucky.Lucky;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public abstract class BaseResource {
    public abstract void process(LuckyReader reader, BaseLoader loader);

    public abstract String getPath();

    protected void logError(Exception e) {
        Lucky.error(e, "Error processing resource: " + this.getPath());
    }

    public boolean isOptional() {
        return false;
    }

    public boolean postInit() {
        return false;
    }
}
