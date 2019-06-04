package mod.lucky.world;

import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class DelayLuckyDrop {
    private long ticksRemaining;
    private DropProcessData processData;
    private DropProcessor dropProcessor;
    private boolean finished;

    public DelayLuckyDrop(
        DropProcessor dropProcessor, DropProcessData processData, long ticksRemaining) {
        this.dropProcessor = dropProcessor;
        this.processData = processData;
        this.ticksRemaining = ticksRemaining;
        this.finished = false;
    }

    public void update() {
        try {
            this.ticksRemaining--;
            if (this.ticksRemaining <= 0) {
                this.dropProcessor.processDelayDrop(this.processData);
                this.finished = true;
            }
        } catch (Exception e) {
            System.err.println(
                "Lucky Block: Error processing delay drop: "
                    + this.processData.getDropSingle().toString());
            e.printStackTrace();
            this.finished = true;
        }
    }

    public void setDropprocessor(DropProcessor dropProcessor) {
        this.dropProcessor = dropProcessor;
    }

    public DropProcessData getProcessData() {
        return this.processData;
    }

    public long getTicksRemaining() {
        return this.ticksRemaining;
    }

    public boolean finished() {
        return this.finished;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound mainTag = new NBTTagCompound();
        mainTag.setTag("processData", this.processData.writeToNBT());
        mainTag.setLong("ticksRemaining", this.ticksRemaining);
        return mainTag;
    }

    public void readFromNBT(NBTTagCompound tagCompound, World world) {
        this.processData = new DropProcessData(world);
        this.processData.readFromNBT(tagCompound.getCompoundTag("processData"));
        this.ticksRemaining = tagCompound.getLong("ticksRemaining");
    }
}
