package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class DropFuncBlock extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        IBlockState blockState = drop.getBlockState();
        if (drop.getPropertyBoolean("blockUpdate"))
            processData.getWorld().setBlockState(drop.getBlockPos(), blockState, 3);
        else
            setBlock(processData.getWorld(), blockState, drop.getBlockPos(),
                drop.getPropertyNBT("NBTTag"),
                drop.getPropertyBoolean("blockUpdate"));

        setTileEntity(processData.getWorld(), blockState, drop.getBlockPos(),
            drop.getPropertyNBT("NBTTag"));
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "tileEntity", NBTTagCompound.class, null);
        DropSingle.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
        DropSingle.setReplaceProperty("meta", "damage");
        DropSingle.setReplaceProperty("state", "damage");
        DropSingle.setReplaceProperty("tileEntity", "NBTTag");
    }

    @Override
    public String getType() {
        return "block";
    }

    public static void setBlock(World world, IBlockState state, BlockPos pos, boolean update) {
        setBlock(world, state, pos, null, update);
    }

    public static void setBlock(World world, IBlockState state, BlockPos pos,
        NBTTagCompound tileEntity, boolean update) {

        Chunk chunk = world.getChunk(pos);
        ChunkSection storageArray = chunk.getSections()[pos.getY() >> 4];
        if (storageArray == null) {
            ChunkSection newSection = new ChunkSection(
                pos.getY() >> 4 << 4, world.dimension.hasSkyLight());
            storageArray = chunk.getSections()[pos.getY() >> 4] = newSection;
        }

        if (storageArray.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)
            != state.getBlock()) {

            IBlockState oldState = world.getBlockState(pos);
            storageArray.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
            chunk.setModified(true);
            world.markAndNotifyBlock(pos, chunk, oldState, state, 3);
            world.checkLight(pos);
            if (update) world.markAndNotifyBlock(pos, chunk, state, oldState, 3);
        }

        if (tileEntity != null && state.getBlock().hasTileEntity(state)) {
            setTileEntity(world, state, pos, tileEntity);
        }
    }

    public static void setTileEntity(World world, IBlockState state,
        BlockPos pos, NBTTagCompound tileEntityData) {

        if (tileEntityData != null && state.getBlock().hasTileEntity(state)) {
            world.removeTileEntity(pos);
            BlockPos chunkPos = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);

            TileEntity tileEntity = state.getBlock().createTileEntity(state, world);
            tileEntity.read(tileEntityData);
            world.setTileEntity(pos, tileEntity);
            tileEntity.updateContainingBlockInfo();
        }
    }
}
