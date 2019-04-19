package mod.lucky.drop.func;

import com.google.common.collect.Lists;
import java.util.List;
import mod.lucky.drop.DropProperties;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;

public class DropFuncParticle extends DropFunction {
  @Override
  public void process(DropProcessData processData) {
    DropProperties drop = processData.getDropProperties();
    String particleName = drop.getPropertyString("ID");
    EnumParticleTypes particle = null;
    for (EnumParticleTypes particleType : EnumParticleTypes.values()) {
      if (particleName.startsWith(particleType.getParticleName())
          && !particleName.equals("splashpotion")) {
        particle = particleType;
        break;
      }
    }

    if (processData.getWorld() instanceof WorldServer) {
      WorldServer worldServer = (WorldServer) processData.getWorld();

      if (particle != null) {
        int[] arguments = new int[particle.getArgumentCount()];

        String[] astring1 = particleName.split("_", 3);

        for (int l = 1; l < astring1.length; ++l) {
          try {
            arguments[l - 1] = Integer.parseInt(astring1[l]);
          } catch (NumberFormatException numberformatexception) {
            continue;
          }
        }

        float posX = drop.getPropertyFloat("posX");
        float posY = drop.getPropertyFloat("posY");
        float posZ = drop.getPropertyFloat("posZ");
        int particleAmount = drop.getPropertyInt("particleAmount");
        float length = drop.getPropertyFloat("length");
        float height = drop.getPropertyFloat("height");
        float width = drop.getPropertyFloat("width");

        worldServer.spawnParticle(
            particle, true, posX, posY, posZ, particleAmount, length, height, width, 0, arguments);
      } else {
        int id =
            ValueParser.getString(particleName, processData).equals("splashpotion")
                ? 2002
                : ValueParser.getInteger(particleName, processData);
        int damage = 0;
        if (id == 2002) {
          if (drop.hasProperty("potion")) {
            List<PotionEffect> effectList = Lists.<PotionEffect>newArrayList();
            PotionType potionType =
                PotionType.getPotionTypeForName(drop.getPropertyString("potion"));
            effectList.addAll(potionType.getEffects());
            damage = PotionUtils.getPotionColorFromEffectList(effectList);
          } else damage = drop.getPropertyInt("damage");
        }
        worldServer.playEvent(id, drop.getBlockPos(), damage);
      }
    }
  }

  @Override
  public void registerProperties() {
    DropProperties.setDefaultProperty(this.getType(), "length", Float.class, 0.0F);
    DropProperties.setDefaultProperty(this.getType(), "height", Float.class, 0.0F);
    DropProperties.setDefaultProperty(this.getType(), "width", Float.class, 0.0F);
    DropProperties.setDefaultProperty(this.getType(), "size", String.class, "(0.0,0.0,0.0)");
    DropProperties.setDefaultProperty(this.getType(), "particleAmount", Integer.class, 1);
    DropProperties.setDefaultProperty(this.getType(), "potion", String.class, "poison");
  }

  @Override
  public String getType() {
    return "particle";
  }
}
