package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import net.minecraft.world.Explosion;

public class DropFuncExplosion extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        processData.getWorld().createExplosion(
            null,
            drop.getPropertyInt("posX") + 0.5,
            drop.getPropertyInt("posY") + 0.5,
            drop.getPropertyInt("posZ") + 0.5,
            drop.getPropertyInt("damage"),
            drop.getPropertyBoolean("fire"),
            Explosion.Mode.DESTROY);
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "damage", Integer.class, 3);
        DropSingle.setDefaultProperty(this.getType(), "fire", Boolean.class, false);
        DropSingle.setReplaceProperty("radius", "damage");
    }

    @Override
    public String getType() {
        return "explosion";
    }
}
