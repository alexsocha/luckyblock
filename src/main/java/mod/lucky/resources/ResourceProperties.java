package mod.lucky.resources;

import mod.lucky.drop.value.ValueParser;
import mod.lucky.resources.loader.BaseLoader;
import mod.lucky.util.LuckyReader;
import mod.lucky.world.LuckyTickHandler;

public class ResourceProperties extends BaseResource {
    @Override
    public void process(LuckyReader reader, BaseLoader loader) {
        try {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                String name = curLine.substring(0, curLine.indexOf('='));
                String value = curLine.substring(curLine.indexOf('=') + 1);

                if (name.equals("doDropsOnCreativeMode"))
                    loader.getBlock().setDoCreativeDrops(ValueParser.getBoolean(value));
                if (name.equals("showUpdateMessage"))
                    LuckyTickHandler.setShowUpdateMessage(ValueParser.getBoolean(value));
            }
        } catch (Exception e) { this.logError(e); }
    }

    @Override
    public String getPath() { return "properties.txt"; }
}
