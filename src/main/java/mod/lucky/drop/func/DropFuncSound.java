package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DropFuncSound extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String soundId = drop.getPropertyString("ID");

        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(
            new ResourceLocation(soundId));
        if (soundEvent == null) {
            Lucky.error(null, "Invalid sound event: " + soundId);
            return;
        }

        SPacketSoundEffect packet = new SPacketSoundEffect(
            soundEvent,
            SoundCategory.BLOCKS,
            processData.getHarvestPos().x,
            processData.getHarvestPos().y,
            processData.getHarvestPos().z,
            drop.getPropertyFloat("volume"),
            drop.getPropertyFloat("pitch"));

        if (processData.getPlayer() instanceof EntityPlayerMP)
            ((EntityPlayerMP) processData.getPlayer())
                .connection.sendPacket(packet);

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
