package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class DropFuncTime extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String id = drop.getPropertyString("ID");
        long time = id.equals("day") ? 1000
            : (id.equals("night") ? 13000
            : ValueParser.getInteger(id));
        processData.getWorld().setTotalWorldTime(time);
    }

    @Override
    public String getType() {
        return "time";
    }
}
