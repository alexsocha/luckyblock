package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class DropFuncBlock extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        IBlockState blockState = drop.getBlockState();
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
        DropSingle.setDefaultProperty(this.getType(), "tileEntity", NBTTagCompound.class, null);
        DropSingle.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
        DropSingle.setDefaultProperty(this.getType(), "state", NBTTagCompound.class, true);
        DropSingle.setReplaceProperty("tileEntity", "NBTTag");
    }

    @Override
    public String getType() {
        return "block";
    }

    public static void setBlock(IWorld world, IBlockState state, BlockPos pos, boolean update) {
        setBlock(world, state, pos, null, update);
    }

    public static void setBlock(IWorld world, IBlockState state, BlockPos pos,
                                NBTTagCompound tileEntity, boolean update) {

        if (world.getBlockState(pos) != state)
            world.setBlockState(pos, state, 2);

        if (update && world instanceof World)
            ((World) world).markAndNotifyBlock(pos, ((World) world).getChunk(pos),
                world.getBlockState(pos), state, 3);

        if (tileEntity != null && state.getBlock().hasTileEntity(state)) {
            setTileEntity(world, state, pos, tileEntity);
        }
    }

    public static void setTileEntity(IWorld world, IBlockState state,
        BlockPos pos, NBTTagCompound tileEntityData) {

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            tileEntity.handleUpdateTag(tileEntityData);
            if (world instanceof World)
                ((World) world).setTileEntity(pos, tileEntity);
        } else {
            Lucky.error(null, "Error setting tile entity for block: " + state);
        }

        /*
        if (tileEntityData != null && state.getBlock().hasTileEntity(state)) {
            world.removeTileEntity(pos);

            TileEntity tileEntity = state.getBlock().createTileEntity(state, world);

            if (tileEntity == null) {
                Lucky.error(null, "Invalid tile entity '" + tileEntityData
                    + "' for block '" + state);
                return;
            }

            tileEntity.read(tileEntityData);
            world.setTileEntity(pos, tileEntity);
            tileEntity.updateContainingBlockInfo();
        }
         */
    }
}
