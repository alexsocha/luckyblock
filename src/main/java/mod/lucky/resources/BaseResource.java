package mod.lucky.resources;

import mod.lucky.Lucky;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;

public abstract class BaseResource {
    public abstract void process(LuckyReader reader, BaseLoader loader);

    public abstract String getDirectory();

    protected void logError() {
        Lucky.LOGGER.error("Error processing resource: " + this.getDirectory());
    }

    public boolean isOptional() {
        return false;
    }

    public boolean postInit() {
        return false;
    }
}
