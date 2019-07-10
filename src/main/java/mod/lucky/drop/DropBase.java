package mod.lucky.drop;

import mod.lucky.drop.func.DropProcessData;
import net.minecraft.nbt.CompoundNBT;

public abstract class DropBase {
    public abstract DropBase initialize(DropProcessData processData);

    public abstract void readFromString(String string);

    public abstract String writeToString();

    public abstract void readFromNBT(CompoundNBT tagCompound);

    public abstract CompoundNBT writeToNBT();
}
