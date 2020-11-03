package mod.lucky.world;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
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
            Lucky.error(e, "Error processing delay drop: "
                    + this.processData.getDropSingle().toString());
            this.finished = true;
        }
    }

    public DropProcessData getProcessData() {
        return this.processData;
    }

    public boolean finished() {
        return this.finished;
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT mainTag = new CompoundNBT();
        mainTag.put("processData", this.processData.writeToNBT());
        mainTag.putLong("ticksRemaining", this.ticksRemaining);
        return mainTag;
    }

    public void readFromNBT(CompoundNBT tagCompound, IWorld world) {
        this.processData = new DropProcessData(world);
        this.processData.readFromNBT(tagCompound.getCompound("processData"));
        this.ticksRemaining = tagCompound.getLong("ticksRemaining");
    }
}
