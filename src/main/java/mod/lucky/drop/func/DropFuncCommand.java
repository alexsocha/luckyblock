package mod.lucky.drop.func;

import mod.lucky.command.LuckyCommandLogic;
import mod.lucky.drop.DropProperties;

public class DropFuncCommand extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropProperties drop = processData.getDropProperties();
        LuckyCommandLogic luckyCommandLogic = new LuckyCommandLogic();
        luckyCommandLogic.setWorld(processData.getWorld());
        luckyCommandLogic.setPosition(drop.getBlockPos());
        luckyCommandLogic.setCommand(drop.getPropertyString("ID"));
        luckyCommandLogic.setName(drop.getPropertyString("commandSender"));
        luckyCommandLogic.setDoOutput(drop.getPropertyBoolean("displayOutput"));
        luckyCommandLogic.executeCommand();
    }

    @Override
    public void registerProperties() {
        DropProperties.setDefaultProperty(this.getType(), "commandSender", String.class, "@");
        DropProperties.setDefaultProperty(this.getType(), "displayOutput", Boolean.class, false);
    }

    @Override
    public String getType() {
        return "command";
    }
}
