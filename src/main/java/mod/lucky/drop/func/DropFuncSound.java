package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DropFuncSound extends DropFunc {
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

        SPlaySoundEffectPacket packet = new SPlaySoundEffectPacket(
            soundEvent,
            SoundCategory.BLOCKS,
            processData.getHarvestPos().x,
            processData.getHarvestPos().y,
            processData.getHarvestPos().z,
            drop.getPropertyFloat("volume"),
            drop.getPropertyFloat("pitch"));

        if (processData.getPlayer() instanceof ServerPlayerEntity)
            ((ServerPlayerEntity) processData.getPlayer())
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
