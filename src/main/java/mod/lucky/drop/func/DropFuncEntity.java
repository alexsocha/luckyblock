package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public class DropFuncEntity extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();

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
        if (id.equals("lightning_bolt") || id.equals("LightningBolt")) {
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

    private static Entity spawnEntity(DropProcessData processData, NBTTagCompound tag,
        World world, double posX, double posY, double posZ) {

        // adjust height
        for (int y = 0; y < 10; y++) {
            if (processData.getWorld().isAirBlock(new BlockPos(posX, posY + y, posZ))) {
                posY += y;
                break;
            }
        }

        if (tag.hasKey("Pos")) {
            NBTTagList posList = tag.getList("Pos", Constants.NBT.TAG_DOUBLE);
            posX = posList.getDouble(0);
            posY = posList.getDouble(1);
            posZ = posList.getDouble(2);
        }

        Lucky.LOGGER.info(tag);
        Entity entity = AnvilChunkLoader.readWorldEntityPos(tag, world, posX, posY, posZ, true);
        if (entity == null) return null;

        UUID playerUUID = processData.getPlayer().getUniqueID();
        if (entity instanceof EntityFallingBlock && !tag.hasKey("Time"))
            ((EntityFallingBlock) entity).fallTime = 1;
        else if (entity instanceof EntityLuckyProjectile)
            ((EntityLuckyProjectile) entity).shootingEntity = playerUUID;
        else if (entity instanceof EntityArrow)
            ((EntityArrow) entity).shootingEntity = playerUUID;


        // randomize entity
        if (entity instanceof EntityLiving
            && processData.getProcessType() != DropProcessData.EnumProcessType.LUCKY_STRUCT
            && !tag.hasKey("Passengers")) {

            ((EntityLiving) entity).onInitialSpawn(
                world.getDifficultyForLocation(new BlockPos(entity)),
                null, null);
            ((EntityLiving) entity).readAdditional(tag);
        }

        return entity;
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "NBTTag", NBTTagCompound.class, null);
        DropSingle.setDefaultProperty(this.getType(), "posX", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "posY", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "posZ", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "posOffsetX", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "posOffsetY", Float.class, 0.0F);
        DropSingle.setDefaultProperty(this.getType(), "posOffsetZ", Float.class, 0.0F);
    }

    @Override
    public String getType() {
        return "entity";
    }
}
