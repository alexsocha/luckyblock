package mod.lucky.drop.func;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.util.LuckyUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.world.ServerWorld;

public class DropFuncCommand extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();

        String command = drop.getPropertyString("ID");
        boolean displayOutput = drop.getPropertyBoolean("displayOutput");
        String name = drop.getPropertyString("commandSender");

        CommandSource commandSource = LuckyUtils.makeCommandSource(
            (ServerWorld) processData.getWorld(),
            LuckyUtils.toVec3d(drop.getBlockPos()),
            displayOutput, name);

        try {
            commandSource.getServer().getCommandManager().handleCommand(commandSource, command);
        } catch (Exception e) {
            Lucky.error(e, "Invalid command: " + command);
        }
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "commandSender", String.class, "Lucky Block");
        DropSingle.setDefaultProperty(this.getType(), "displayOutput", Boolean.class, false);
    }

    @Override
    public String getType() { return "command"; }
}
