package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class DropFuncSound extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        if (processData.getPlayer() instanceof EntityPlayerMP)
            ((EntityPlayerMP) processData.getPlayer())
                .connection.sendPacket(
                new SPacketSoundEffect(
                    SoundEvent.REGISTRY.getObject(new ResourceLocation(drop.getPropertyString("ID"))),
                    SoundCategory.BLOCKS,
                    processData.getHarvestPos().x,
                    processData.getHarvestPos().y,
                    processData.getHarvestPos().z,
                    drop.getPropertyFloat("volume"),
                    drop.getPropertyFloat("pitch")));
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "volume", Float.class, 1.0F);
        DropSingle.setDefaultProperty(this.getType(), "pitch", Float.class, 1.0F);
    }

    @Override
    public String getType() {
        return "sound";
    }
}
