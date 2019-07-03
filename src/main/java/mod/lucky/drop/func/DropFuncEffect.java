package mod.lucky.drop.func;

import java.util.Iterator;
import java.util.List;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ForgeRegistries;

public class DropFuncEffect extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();

        Entity target = null;
        AxisAlignedBB effectBox =
            new AxisAlignedBB(drop.getBlockPos(), drop.getBlockPos())
                .expand(
                    drop.getPropertyInt("range") * 2,
                    drop.getPropertyInt("range") * 2,
                    drop.getPropertyInt("range") * 2);
        if (drop.hasProperty("target") && !drop.hasProperty("range"))
            target =
                drop.getPropertyString("target").equals("player")
                    ? processData.getPlayer()
                    : (drop.getPropertyString("target").equals("hitEntity")
                    ? processData.getHitEntity()
                    : null);
        if (!drop.hasProperty("target") && !drop.hasProperty("range")) target = processData.getPlayer();
        if (drop.getPropertyString("target").equals("hitEntity") && processData.getHitEntity() == null)
            return;

        int potionEffectId = -1;
        String effectID = drop.getPropertyString("ID");
        if (!(effectID.equals("special_fire")) && !(effectID.equals("special_knockback"))) {
            try {
                potionEffectId = ValueParser.getInteger(effectID);
            } catch (Exception e) {
                Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(effectID));
                if (potion == null) {
                    Lucky.error(null, "Invalid potion effect: " + effectID);
                    return;
                }
                potionEffectId = Potion.getIdFromPotion(potion);
            }
        }

        if (target != null) {
            if (effectID.equals("special_fire")) this.specialEffectFire(processData, target);
            else if (effectID.equals("special_knockback"))
                this.specialEffectKnockback(processData, target);
            else this.potionEffect(processData, target, potionEffectId);
        } else if (effectBox != null) {
            List list1 = processData.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, effectBox);
            if (!list1.isEmpty()) {
                Iterator iterator = list1.iterator();

                while (iterator.hasNext()) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) iterator.next();
                    if (processData.getDropSingle().getPropertyBoolean("excludePlayer")
                        && entityLivingBase == processData.getPlayer()) continue;
                    double distance =
                        processData
                            .getDropSingle()
                            .getVecPos()
                            .distanceTo(entityLivingBase.getPositionVector());

                    if (distance <= drop.getPropertyFloat("range")) {
                        if (effectID.equals("special_fire"))
                            this.specialEffectFire(processData, entityLivingBase);
                        else if (effectID.equals("special_knockback"))
                            this.specialEffectKnockback(processData, entityLivingBase);
                        else this.potionEffect(processData, entityLivingBase, potionEffectId);
                    }
                }
            }
        }
    }

    private void potionEffect(DropProcessData processData, Entity entity, int potionEffectId) {
        Potion potion = Potion.getPotionById(potionEffectId);
        int duration = (int) (processData.getDropSingle().getPropertyFloat("duration") * 20.0);
        if (potion.isInstant()) duration = 1;

        PotionEffect potionEffect =
            new PotionEffect(
                potion, duration, processData.getDropSingle().getPropertyInt("amplifier"));
        if (entity instanceof EntityLivingBase)
            ((EntityLivingBase) entity).addPotionEffect(potionEffect);
    }

    private void specialEffectFire(DropProcessData processData, Entity entity) {
        entity.setFire(processData.getDropSingle().getPropertyInt("duration"));
    }

    private void specialEffectKnockback(DropProcessData processData, Entity entity) {
        Vec3d dropPos = processData.getDropSingle().getVecPos();
        float yawAngle =
            processData.getDropSingle().hasProperty("directionYaw")
                ? processData.getDropSingle().getPropertyFloat("directionYaw")
                : (float)
                Math.toDegrees(Math.atan2((entity.posX - dropPos.x) * -1, entity.posZ - dropPos.z));
        float pitchAngle = processData.getDropSingle().getPropertyFloat("directionPitch");
        float power = processData.getDropSingle().getPropertyFloat("power");

        if (!processData.getDropSingle().hasProperty("target")
            && dropPos.distanceTo(entity.getPositionVector()) < 0.01) {
            pitchAngle = -90;
            power *= 0.5;
        }

        entity.motionX =
            -MathHelper.sin(yawAngle / 180.0F * (float) Math.PI)
                * MathHelper.cos(pitchAngle / 180.0F * (float) Math.PI)
                * power;
        entity.motionZ =
            MathHelper.cos(yawAngle / 180.0F * (float) Math.PI)
                * MathHelper.cos(pitchAngle / 180.0F * (float) Math.PI)
                * power;
        entity.motionY = -MathHelper.sin(pitchAngle / 180.0F * (float) Math.PI) * power;
        entity.velocityChanged = true;
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "duration", Integer.class, 30);
        DropSingle.setDefaultProperty(this.getType(), "amplifier", Integer.class, 0);
        DropSingle.setDefaultProperty(this.getType(), "target", String.class, "player");
        DropSingle.setDefaultProperty(this.getType(), "excludePlayer", Boolean.class, false);
        DropSingle.setDefaultProperty(this.getType(), "range", Float.class, 4);
        DropSingle.setDefaultProperty(this.getType(), "power", Float.class, 1);
        DropSingle.setDefaultProperty(this.getType(), "directionYaw", Float.class, 0);
        DropSingle.setDefaultProperty(this.getType(), "directionPitch", Float.class, -30);
    }

    @Override
    public String getType() {
        return "effect";
    }
}
