package mod.lucky.drop.func;

import mod.lucky.drop.DropProperties;

public class DropFuncExplosion extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropProperties drop = processData.getDropProperties();
        processData
            .getWorld()
            .newExplosion(
                null,
                drop.getPropertyInt("posX") + 0.5,
                drop.getPropertyInt("posY") + 0.5,
                drop.getPropertyInt("posZ") + 0.5,
                drop.getPropertyInt("damage"),
                drop.getPropertyBoolean("fire"),
                true);
    }

    @Override
    public void registerProperties() {
        DropProperties.setDefaultProperty(this.getType(), "damage", Integer.class, 3);
        DropProperties.setDefaultProperty(this.getType(), "fire", Boolean.class, false);
        DropProperties.setReplaceProperty("radius", "damage");
    }

    @Override
    public String getType() {
        return "explosion";
    }
}
