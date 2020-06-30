package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ServerWorld;

public class DropFuncTime extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String id = drop.getPropertyString("ID");
        long time = id.equals("day") ? 1000
            : (id.equals("night") ? 13000
            : ValueParser.getInteger(id));

        if (processData.getWorld() instanceof ServerWorld)
            ((ServerWorld) processData.getWorld()).func_241114_a_(time); // setDayTime()
        if (processData.getWorld() instanceof ClientWorld)
            ((ClientWorld) processData.getWorld()).setDayTime(time);
    }

    @Override
    public String getType() {
        return "time";
    }
}
