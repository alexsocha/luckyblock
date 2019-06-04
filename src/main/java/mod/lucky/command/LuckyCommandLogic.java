package mod.lucky.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class LuckyCommandLogic implements ICommandSource {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private int successCount;
    private ITextComponent outputMessage = null;
    private boolean doOutput = false;
    private String command = "";
    private String commandSender = "@";
    public BlockPos position = new BlockPos(0, 0, 0);
    public World world;

    public int getSuccessCount() {
        return this.successCount;
    }

    public ITextComponent getOutputMessage() {
        return this.outputMessage;
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

    public void setDoOutput(boolean doOutput) {
        this.doOutput = doOutput;
    }

    public boolean getDoOutput() {
        return this.doOutput;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void executeCommand() {
        if (this.world.isRemote) {
            this.successCount = 0;
        }

        MinecraftServer minecraftserver = this.getServer();
        if (minecraftserver != null) {
            ICommandManager icommandmanager = minecraftserver.getCommandManager();
            this.successCount = icommandmanager.executeCommand(this, this.command);
        } else {
            this.successCount = 0;
        }
    }

    @Override
    public String getName() {
        return this.commandSender;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }

    public void setName(String name) {
        this.commandSender = name;
    }

    @Override
    public void sendMessage(ITextComponent message) {
        if (this.doOutput && this.getEntityWorld() != null && !this.getEntityWorld().isRemote) {
            this.outputMessage =
                (new TextComponentString("[" + dateFormat.format(new Date()) + "] "))
                    .appendSibling(message);
        }
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return false;
    }

    @Override
    public boolean shouldReceiveErrors() {
        return false;
    }

    @Override
    public boolean allowLogging() {
        return false;
    }

    public void setOutputMessage(ITextComponent message) {
        this.outputMessage = message;
    }

    @Override
    public World getEntityWorld() {
        return this.world;
    }

    @Override
    public BlockPos getPosition() {
        return this.position;
    }

    public BlockPos setPosition(BlockPos pos) {
        return this.position = pos;
    }

    @Override
    public Vec3d getPositionVector() {
        return new Vec3d(this.position.getX(), this.position.getY(), this.position.getZ());
    }

    @Override
    public Entity getCommandSenderEntity() {
        return null;
    }

    @Override
    public boolean sendCommandFeedback() {
        return this.doOutput;
    }

    @Override
    public void setCommandStat(Type type, int amount) {
    }

    @Override
    public MinecraftServer getServer() {
        return this.world.getMinecraftServer();
    }
}
