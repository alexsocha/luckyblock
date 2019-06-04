package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class DropFuncBlock extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        IBlockState blockState = drop.getBlockState();
        if (drop.getPropertyBoolean("blockUpdate") == true)
            processData.getWorld().setBlockState(drop.getBlockPos(), blockState, 3);
        else
            setBlock(
                processData.getWorld(),
                blockState,
                drop.getBlockPos(),
                drop.getPropertyNBT("NBTTag"),
                drop.getPropertyBoolean("blockUpdate"));
        setTileEntity(
            processData.getWorld(), blockState, drop.getBlockPos(), drop.getPropertyNBT("NBTTag"));
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

    public static void setBlock(
        World world, IBlockState state, BlockPos pos, NBTTagCompound tileEntity, boolean update) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        ExtendedBlockStorage storageArray = chunk.getBlockStorageArray()[pos.getY() >> 4];
        if (storageArray == null)
            storageArray =
                chunk.getBlockStorageArray()[pos.getY() >> 4] =
                    new ExtendedBlockStorage(pos.getY() >> 4 << 4, world.provider.hasSkyLight());

        if (storageArray.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15) != state.getBlock()) {
            IBlockState oldState = world.getBlockState(pos);
            storageArray.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
            chunk.setModified(true);
            world.markAndNotifyBlock(pos, chunk, oldState, state, 3);
            world.checkLight(pos);
            if (update) world.markAndNotifyBlock(pos, chunk, state, oldState, 3);
        }

        if (tileEntity != null && state.getBlock().hasTileEntity(state)) {
            world.removeTileEntity(pos);
            BlockPos chunkPos = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            TileEntity blockTileEntity = chunk.getTileEntity(chunkPos, Chunk.EnumCreateEntityType.CHECK);

            blockTileEntity = state.getBlock().createTileEntity(world, state);
            blockTileEntity.readFromNBT(tileEntity);
            world.setTileEntity(pos, blockTileEntity);
            blockTileEntity.updateContainingBlockInfo();
        }
    }

    public static void setTileEntity(
        World world, IBlockState state, BlockPos pos, NBTTagCompound tileEntity) {
        if (tileEntity != null && state.getBlock().hasTileEntity(state)) {
            Chunk chunk = world.getChunkFromBlockCoords(pos);
            ExtendedBlockStorage storageArray = chunk.getBlockStorageArray()[pos.getY() >> 4];
            if (storageArray == null)
                storageArray =
                    chunk.getBlockStorageArray()[pos.getY() >> 4] =
                        new ExtendedBlockStorage(pos.getY() >> 4 << 4, world.provider.hasSkyLight());

            world.removeTileEntity(pos);
            BlockPos chunkPos = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            TileEntity blockTileEntity = chunk.getTileEntity(chunkPos, Chunk.EnumCreateEntityType.CHECK);

            blockTileEntity = state.getBlock().createTileEntity(world, state);
            blockTileEntity.readFromNBT(tileEntity);
            world.setTileEntity(pos, blockTileEntity);
            blockTileEntity.updateContainingBlockInfo();
        }
    }
}
