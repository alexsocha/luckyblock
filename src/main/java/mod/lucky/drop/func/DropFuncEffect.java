package mod.lucky.drop.func;

import java.util.Iterator;
import java.util.List;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
                Effect potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(effectID));
                if (potion == null) {
                    Lucky.error(null, "Invalid potion effect: " + effectID);
                    return;
                }
                potionEffectId = Effect.getId(potion);
            }
        }

        if (target != null) {
            if (effectID.equals("special_fire")) this.specialEffectFire(processData, target);
            else if (effectID.equals("special_knockback"))
                this.specialEffectKnockback(processData, target);
            else this.potionEffect(processData, target, potionEffectId);
        } else if (effectBox != null) {
            List list1 = processData.getWorld().getEntitiesWithinAABB(LivingEntity.class, effectBox);
            if (!list1.isEmpty()) {
                Iterator iterator = list1.iterator();

                while (iterator.hasNext()) {
                    LivingEntity entity = (LivingEntity) iterator.next();
                    if (processData.getDropSingle().getPropertyBoolean("excludePlayer")
                        && entity == processData.getPlayer()) continue;
                    double distance =
                        processData
                            .getDropSingle()
                            .getVecPos()
                            .distanceTo(entity.getPositionVec());

                    if (distance <= drop.getPropertyFloat("range")) {
                        if (effectID.equals("special_fire"))
                            this.specialEffectFire(processData, entity);
                        else if (effectID.equals("special_knockback"))
                            this.specialEffectKnockback(processData, entity);
                        else this.potionEffect(processData, entity, potionEffectId);
                    }
                }
            }
        }
    }

    private void potionEffect(DropProcessData processData, Entity entity, int potionEffectId) {
        Effect potion = Effect.get(potionEffectId);
        int duration = (int) (processData.getDropSingle().getPropertyFloat("duration") * 20.0);
        if (potion.isInstant()) duration = 1;

        EffectInstance potionEffect =
            new EffectInstance(
                potion, duration, processData.getDropSingle().getPropertyInt("amplifier"));
        if (entity instanceof LivingEntity)
            ((LivingEntity) entity).addPotionEffect(potionEffect);
    }

    private void specialEffectFire(DropProcessData processData, Entity entity) {
        entity.setFire(processData.getDropSingle().getPropertyInt("duration"));
    }

    private void specialEffectKnockback(DropProcessData processData, Entity entity) {
        Vector3d dropPos = processData.getDropSingle().getVecPos();
        Vector3d entityPos = entity.getPositionVec();
        float yawAngle =
            processData.getDropSingle().hasProperty("directionYaw")
                ? processData.getDropSingle().getPropertyFloat("directionYaw")
                : (float)
                Math.toDegrees(Math.atan2((entityPos.x - dropPos.x) * -1, entityPos.z - dropPos.z));
        float pitchAngle = processData.getDropSingle().getPropertyFloat("directionPitch");
        float power = processData.getDropSingle().getPropertyFloat("power");

        if (!processData.getDropSingle().hasProperty("target")
            && dropPos.distanceTo(entity.getPositionVec()) < 0.01) {
            pitchAngle = -90;
            power *= 0.5;
        }

        entity.setMotion(new Vector3d(
            -MathHelper.sin(yawAngle / 180.0F * (float) Math.PI)
                * MathHelper.cos(pitchAngle / 180.0F * (float) Math.PI)
                * power,

            -MathHelper.sin(pitchAngle / 180.0F * (float) Math.PI) * power,

            MathHelper.cos(yawAngle / 180.0F * (float) Math.PI)
                * MathHelper.cos(pitchAngle / 180.0F * (float) Math.PI)
                * power));
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
