package mod.lucky.structure;

import mod.lucky.drop.func.DropFuncBlock;
import mod.lucky.util.LuckyUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class StructureUtils {
    private static int normalizeRotation(int rotation) {
        rotation %= 4;
        if (rotation < 0) rotation += 4;
        return rotation;
    }

    public static Vec3d rotatePos(Vec3d pos, Vec3d centerPos, int rotation) {
        rotation = normalizeRotation(rotation);

        double posX = pos.x - centerPos.x;
        double posY = pos.y - centerPos.y;
        double posZ = centerPos.z - pos.z;

        for (int i = 0; i < rotation; ++i) {
            double x = posX;
            double z = posZ;
            posX = z;
            posZ = -x;
        }
        return new Vec3d(posX + centerPos.x, posY + centerPos.y, centerPos.z - posZ);
    }

    public static Rotation parseRotation(int rotation) {
        return Rotation.values()[rotation];
    }

    public static Vec3d getWorldPos(
        Vec3d structPos, Vec3d structCenter, Vec3d harvestPos, int rotation) {
        return rotatePos(
            harvestPos.add(structPos).subtract(structCenter),
            harvestPos, rotation);
    }

    public static BlockPos getWorldPos(
        BlockPos posInStruct, Vec3d structCenter, Vec3d harvestPos, int rotation) {
        return new BlockPos(getWorldPos(LuckyUtils.toVec3d(posInStruct),
            structCenter, harvestPos, rotation));
    }

    @Nullable
    public static BlockState applyBlockMode(Structure.BlockMode mode, BlockState state) {
        if (mode == Structure.BlockMode.AIR && state.getBlock() != Blocks.AIR)
            return Blocks.AIR.getDefaultState();
        else if (mode == Structure.BlockMode.OVERLAY && state.getBlock() != Blocks.AIR)
            return state;
        else if (mode == Structure.BlockMode.REPLACE) return state;

        return null;
    }

    public static void setBlock(BlockPlacer blockPlacer, BlockState blockState,
        BlockPos posInStruct, Vec3d structCenter, Vec3d harvestPos, int rotation) {

        BlockPos pos = StructureUtils.getWorldPos(
            posInStruct, structCenter, harvestPos, rotation);
        BlockState newBlockState = blockState.rotate(
            blockPlacer.getWorld(), pos, parseRotation(rotation));

        blockPlacer.add(newBlockState, pos);
    }

    public static void fillWithAir(BlockPlacer blockPlacer, BlockPos size,
        Vec3d centerPos, Vec3d harvestPos, int rotation) {

        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    setBlock(blockPlacer,
                        Blocks.AIR.getDefaultState(),
                        new BlockPos(x, y, z), centerPos,
                        harvestPos, rotation);
                }
            }
        }
    }

    public static void setTileEntity(IWorld world, CompoundNBT tileEntity,
        BlockPos structPos, Vec3d structCenter, Vec3d harvestPos, int rotation) {

        BlockPos pos = getWorldPos(structPos, structCenter, harvestPos, rotation);
        DropFuncBlock.setTileEntity(world, world.getBlockState(pos), pos, tileEntity);
    }

    public static void setEntity(World world, Entity entity,
        Vec3d structCenter, Vec3d harvestPos, int rotation) {

        if (!(world instanceof ServerWorld)) return;

        Vec3d pos = getWorldPos(entity.getPositionVector(), structCenter, harvestPos, rotation);
        entity.setPosition(pos.x, pos.y, pos.z);
        entity.rotationYaw = entity.getRotatedYaw(parseRotation(rotation));
        ((ServerWorld) world).summonEntity(entity);
    }

    public static CompoundNBT rotateEntityNBT(
        CompoundNBT entityTag, Vec3d centerPos, int rotation) {

        CompoundNBT newTag = entityTag.copy();
        rotation = normalizeRotation(rotation);
        if (entityTag.contains("Pos")) {
            ListNBT posList = entityTag.getList("Pos", Constants.NBT.TAG_DOUBLE);
            Vec3d entityPos = new Vec3d(
                posList.getDouble(0), posList.getDouble(1), posList.getDouble(2));
            entityPos = rotatePos(entityPos, centerPos, rotation);
            posList = new ListNBT();
            posList.add(new DoubleNBT(entityPos.x));
            posList.add(new DoubleNBT(entityPos.y));
            posList.add(new DoubleNBT(entityPos.z));
            newTag.put("Pos", posList);
        }
        if (entityTag.contains("Motion")) {
            ListNBT motionList = entityTag.getList("Motion", Constants.NBT.TAG_DOUBLE);
            Vec3d entityMotion = new Vec3d(
                motionList.getDouble(0), motionList.getDouble(1), motionList.getDouble(2));
            entityMotion = rotatePos(entityMotion, new Vec3d(0, 0, 0), rotation);
            motionList = new ListNBT();
            motionList.add(new DoubleNBT(entityMotion.x));
            motionList.add(new DoubleNBT(entityMotion.y));
            motionList.add(new DoubleNBT(entityMotion.z));
            newTag.put("Motion", motionList);
        }
        if (entityTag.contains("Rotation")) {
            ListNBT rotationList = entityTag.getList("Rotation", Constants.NBT.TAG_FLOAT);
            float rotYaw = rotationList.getFloat(0);
            float rotPitch = rotationList.getFloat(1);
            rotYaw = (rotYaw + (rotation * 90.0F)) % 360.0F;
            rotationList = new ListNBT();
            rotationList.add(new FloatNBT(rotYaw));
            rotationList.add(new FloatNBT(rotPitch));
            newTag.put("Rotation", rotationList);
        }
        return newTag;
    }
}
