package mod.lucky.drop.func;

import net.minecraft.util.text.TextComponentString;

public class DropFuncMessage extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        processData
            .getPlayer()
            .sendMessage(
                new TextComponentString(processData.getDropProperties().getPropertyString("ID")));
    }

    @Override
    public String getType() {
        return "message";
    }
}
