package mod.lucky.drop.func;

import mod.lucky.drop.DropProperties;
import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DropFuncEntity extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropProperties drop = processData.getDropProperties();

        float posX = drop.getPropertyFloat("posX");
        float posY = drop.getPropertyFloat("posY");
        float posZ = drop.getPropertyFloat("posZ");

        if (posY <= -1) return;

        Entity entity;
        NBTTagCompound nbtTagCompound =
            drop.getPropertyNBT("NBTTag") == null
                ? new NBTTagCompound()
                : drop.getPropertyNBT("NBTTag");

        String id = drop.getPropertyString("ID");
        if (id.equals("LightningBolt")) {
            processData
                .getWorld()
                .addWeatherEffect(
                    new EntityLightningBolt(processData.getWorld(), posX, posY, posZ, false));
            return;
        } else {
            nbtTagCompound.setString("id", id);
            spawnEntity(processData, nbtTagCompound, processData.getWorld(), posX, posY, posZ);
        }
    }

    private static Entity spawnEntity(
        DropProcessData processData,
        NBTTagCompound nbtTagCompound,
        World world,
        double posX,
        double posY,
        double posZ) {
        Entity entity = EntityList.createEntityFromNBT(nbtTagCompound, world);
        if (entity == null) return null;

        if (entity instanceof EntityTippedArrow
            && !nbtTagCompound.hasKey("Potion")
            && !nbtTagCompound.hasKey("CustomPotionEffects"))
            ((EntityTippedArrow) entity).setPotionEffect(new ItemStack(Items.ARROW));

        boolean hasPos = nbtTagCompound.hasKey("Pos");
        boolean hasMotion = nbtTagCompound.hasKey("Motion");
        boolean hasRotation = nbtTagCompound.hasKey("Rotation");
        if (!hasPos) {
            entity.posX = posX;
            entity.posY = posY;
            entity.posZ = posZ;
        }
        if (entity instanceof EntityThrowable && !hasRotation && hasMotion) {
            float sqrt =
                MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
            entity.rotationYaw = (float) (Math.atan2(entity.motionX, entity.motionZ) * 180.0D / Math.PI);
            entity.rotationPitch = (float) (Math.atan2(entity.motionY, sqrt) * 180.0D / Math.PI);
            entity.velocityChanged = true;
        }

        if (entity instanceof EntityFallingBlock && !nbtTagCompound.hasKey("Time"))
            ((EntityFallingBlock) entity).fallTime = 1;
        else if (entity instanceof EntityLuckyProjectile)
            ((EntityLuckyProjectile) entity).shootingEntity = processData.getPlayer();
        else if (entity instanceof EntityArrow)
            ((EntityArrow) entity).shootingEntity = processData.getPlayer();

        // adjust height
        for (int y = 0; y < 10; y++) {
            if (processData
                .getWorld()
                .isAirBlock(new BlockPos(entity.posX, entity.posY + y, entity.posZ))) {
                entity.posY += y;
                break;
            }
        }
        entity.setLocationAndAngles(
            entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

        if (entity instanceof EntityLiving) {
            if (processData.getProcessType() != DropProcessData.EnumProcessType.LUCKY_STRUCT)
                ((EntityLiving) entity)
                    .onInitialSpawn(
                        world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
            ((EntityLiving) entity).readEntityFromNBT(nbtTagCompound);
        }

        if (!world.spawnEntity(entity)) {
            return null;
        } else {
            if (nbtTagCompound.hasKey("Passengers", 9)) {
                NBTTagList nbttaglist = nbtTagCompound.getTagList("Passengers", 10);

                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    Entity entity1 =
                        spawnEntity(processData, nbttaglist.getCompoundTagAt(i), world, posX, posY, posZ);
                    if (entity1 != null) entity1.startRiding(entity, true);
                }
            }

            return entity;
        }
    }

    @Override
    public void registerProperties() {
        DropProperties.setDefaultProperty(this.getType(), "NBTTag", NBTTagCompound.class, null);
        DropProperties.setDefaultProperty(this.getType(), "posX", Float.class, 0.0F);
        DropProperties.setDefaultProperty(this.getType(), "posY", Float.class, 0.0F);
        DropProperties.setDefaultProperty(this.getType(), "posZ", Float.class, 0.0F);
        DropProperties.setDefaultProperty(this.getType(), "posOffsetX", Float.class, 0.0F);
        DropProperties.setDefaultProperty(this.getType(), "posOffsetY", Float.class, 0.0F);
        DropProperties.setDefaultProperty(this.getType(), "posOffsetZ", Float.class, 0.0F);
    }

    @Override
    public String getType() {
        return "entity";
    }
}
