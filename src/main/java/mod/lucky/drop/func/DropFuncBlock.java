package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DropFuncBlock extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        BlockState blockState = drop.getBlockState();
        if (drop.getPropertyBoolean("blockUpdate"))
            processData.getRawWorld().setBlockState(drop.getBlockPos(), blockState, 3);

            if (drop.hasProperty("NBTTag"))
                setTileEntity(processData.getRawWorld(), blockState, drop.getBlockPos(),
                    drop.getPropertyNBT("NBTTag"));

        else
            setBlock(processData.getRawWorld(), blockState, drop.getBlockPos(),
                drop.getPropertyNBT("NBTTag"),
                drop.getPropertyBoolean("blockUpdate"));
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "tileEntity", CompoundNBT.class, null);
        DropSingle.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
        DropSingle.setDefaultProperty(this.getType(), "state", CompoundNBT.class, true);
        DropSingle.setReplaceProperty("tileEntity", "NBTTag");
    }

    @Override
    public String getType() {
        return "block";
    }

    public static void setBlock(IWorld world, BlockState state, BlockPos pos, boolean update) {
        setBlock(world, state, pos, null, update);
    }

    public static void setBlock(IWorld world, BlockState state, BlockPos pos,
        CompoundNBT tileEntity, boolean update) {

        if (world.getBlockState(pos) != state)
            world.setBlockState(pos, state, update ? 3 : 2);

        if (tileEntity != null && state.getBlock().hasTileEntity(state)) {
            setTileEntity(world, state, pos, tileEntity);
        }
    }

    public static void setTileEntity(IWorld world, BlockState state,
        BlockPos pos, CompoundNBT tileEntityData) {

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            tileEntity.handleUpdateTag(state, tileEntityData);
            if (world instanceof World)
                ((World) world).setTileEntity(pos, tileEntity);
        } else {
            Lucky.error(null, "Error setting tile entity for block: " + state);
        }
    }
}
