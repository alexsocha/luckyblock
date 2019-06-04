package mod.lucky.drop.func;

import mod.lucky.command.LuckyCommandLogic;
import mod.lucky.drop.DropSingle;

public class DropFuncCommand extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
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
        DropSingle.setDefaultProperty(this.getType(), "commandSender", String.class, "@");
        DropSingle.setDefaultProperty(this.getType(), "displayOutput", Boolean.class, false);
    }

    @Override
    public String getType() {
        return "command";
    }
}
