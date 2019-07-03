package mod.lucky.drop.func;

import com.google.common.collect.Lists;

import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.WorldServer;

public class DropFuncParticle extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String particleIdFull = drop.getPropertyString("ID");
        String particleId = particleIdFull.split("\\.")[0];
        boolean isEvent = false;
        int eventId = 0;

        ResourceLocation particleResLoc = new ResourceLocation(particleId);

        ParticleType particleType = null;
        // particle registry
        if (IRegistry.field_212632_u.func_212607_c(particleResLoc))
            particleType = IRegistry.field_212632_u.func_212608_b(particleResLoc);

        if (particleType == null) {
            if (ValueParser.getString(
                particleId, processData).equals("splashpotion")) {

                isEvent = true;
                eventId = 2002;
            } else {
                try {
                    eventId = ValueParser.getInteger(particleId, processData);
                    isEvent = true;
                } catch (Exception e) {}
            }
        }

        if (particleType == null && !isEvent) {
            Lucky.error(null, "Invalid particle: " + particleId);
            return;
        }

        if (processData.getWorld() instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) processData.getWorld();

            if (!isEvent) {
                String particleArgs = "";
                IParticleData particleData;

                String[] splitArgs = particleIdFull.split("\\.");
                for (int i = 1; i < splitArgs.length; i++) {
                    particleArgs += " " + splitArgs[i];
                }
                try {
                    StringReader argReader = new StringReader(particleArgs);
                    argReader.setCursor(0);
                    particleData = particleType.getDeserializer()
                        .deserialize(particleType, argReader);
                } catch (CommandSyntaxException e) {
                    Lucky.error(e, "Failed to process particle: " + particleId);
                    return;
                }

                float posX = drop.getPropertyFloat("posX");
                float posY = drop.getPropertyFloat("posY");
                float posZ = drop.getPropertyFloat("posZ");
                int particleAmount = drop.getPropertyInt("particleAmount");
                float length = drop.getPropertyFloat("length");
                float height = drop.getPropertyFloat("height");
                float width = drop.getPropertyFloat("width");

                worldServer.spawnParticle(particleData,
                    posX, posY, posZ,
                    particleAmount,
                    length, height, width, 0);
            } else {
                int damage = 0;
                if (eventId == 2002) {
                    if (drop.hasProperty("potion")) {
                        List<PotionEffect> effectList = Lists.<PotionEffect>newArrayList();
                        PotionType potionType =
                            PotionType.getPotionTypeForName(drop.getPropertyString("potion"));
                        effectList.addAll(potionType.getEffects());
                        damage = PotionUtils.getPotionColorFromEffectList(effectList);
                    } else damage = drop.getPropertyInt("damage");
                }
                worldServer.playEvent(eventId, drop.getBlockPos(), damage);
            }
        }
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "length", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "height", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "width", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "size", String.class, "(0.0,0.0,0.0)");
        DropSingle.setDefaultProperty(this.getType(), "particleAmount", Integer.class, 1);
        DropSingle.setDefaultProperty(this.getType(), "potion", String.class, "poison");
    }

    @Override
    public String getType() {
        return "particle";
    }
}
