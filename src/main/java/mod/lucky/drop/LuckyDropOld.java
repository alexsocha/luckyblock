package mod.lucky.drop;

import net.minecraft.nbt.NBTTagCompound;

public class LuckyDropOld {
    public String type = "drop";
    public String id = "";
    public int damage = 0;
    public int amount = 1;
    public boolean reinitialize = false;
    public boolean relativeToPlayer = false;
    public int posX = 0;
    public int posY = 0;
    public int posZ = 0;
    public int pos2X = 0;
    public int pos2Y = 0;
    public int pos2Z = 0;
    public int centerX = 0, centerY = 0, centerZ = 0;
    public boolean setCenterX = false, setCenterY = false, setCenterZ = false;
    public int rotation = 0;
    public boolean doUpdate = true;
    public int blockMode = 0;
    public int effectDuration = 200;
    public NBTTagCompound nbttag;
    public boolean displayCommandOutput = false;
    public String commandSender = "@";

    public LuckyDropOld() {
    }

    public void setType(String par1) {
        this.type = par1;
    }

    public String getType() {
        return this.type;
    }

    public void setId(String par1) {
        this.id = par1;
    }

    public String getId() {
        return this.id;
    }

    public void setDamage(int par1) {
        this.damage = par1;
    }

    public int getDamage() {
        return this.damage;
    }

    public void setAmount(int par1) {
        this.amount = par1;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setRelativeToPlayer(Boolean par1) {
        this.relativeToPlayer = par1;
    }

    public boolean getRelativeToPlayer() {
        return this.relativeToPlayer;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosX() {
        return this.posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getPosY() {
        return this.posY;
    }

    public void setPosZ(int posZ) {
        this.posZ = posZ;
    }

    public int getPosZ() {
        return this.posZ;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
        this.setCenterX = true;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
        this.setCenterY = true;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
        this.setCenterZ = true;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return this.rotation;
    }

    public boolean getDoUpdate() {
        return this.doUpdate;
    }

    public void setDoUpdate(boolean doUpdate) {
        this.doUpdate = doUpdate;
    }

    public void setEffectDuration(int par1) {
        this.effectDuration = par1 * 20;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public void setNBTTag(NBTTagCompound nbttag) {
        this.nbttag = nbttag;
    }

    public NBTTagCompound getNBTTag() {
        return this.nbttag;
    }

    public void setReinitialize(boolean reinitialize) {
        this.reinitialize = reinitialize;
    }

    public boolean getReinitialize() {
        return this.reinitialize;
    }

    public void setDisplayCommandOutput(boolean displayCommandOutput) {
        this.displayCommandOutput = displayCommandOutput;
    }

    public boolean getDisplayCommandOutput() {
        return this.displayCommandOutput;
    }

    public void setCommandSender(String commandSender) {
        this.commandSender = commandSender;
    }

    public String getCommandSender() {
        return this.commandSender;
    }
}
