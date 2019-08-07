package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;

public class DropFuncTime extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String id = drop.getPropertyString("ID");
        long time = id.equals("day") ? 1000
            : (id.equals("night") ? 13000
            : ValueParser.getInteger(id));

        processData.getWorld().setDayTime(time);
    }

    @Override
    public String getType() {
        return "time";
    }
}
