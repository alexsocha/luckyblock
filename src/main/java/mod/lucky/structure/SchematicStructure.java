package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import mod.lucky.drop.DropProperties;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SchematicStructure extends Structure {
    // Blocks stored [y][z][x]
    private Block[][][] blocks;
    private int[][][] blockData;

    private NBTTagCompound[] entities;
    private NBTTagCompound[] tileEntities;

    @Override
    public void process(DropProcessData processData) {
        DropProperties drop = processData.getDropProperties();
        Vec3d harvestPos = drop.getVecPos();
        int rotation = drop.getPropertyInt("rotation");
        BlockPlacer blockPlacer = new BlockPlacer(processData.getWorld());

        for (int y = 0; y < this.height; y++) {
            for (int z = 0; z < this.width; z++) {
                for (int x = 0; x < this.length; x++) {
                    if (this.blockMode.equals("overlay")) {
                        if (this.blocks[y][z][x] != Blocks.AIR)
                            StructureUtils.setBlock(
                                blockPlacer,
                                this.blocks[y][z][x].getStateFromMeta(this.blockData[y][z][x]),
                                new BlockPos(x, y, z),
                                this.getCenterPos(),
                                harvestPos,
                                rotation);
                    } else if (this.blockMode.equals("air")) {
                        if (this.blocks[y][z][x] != Blocks.AIR)
                            StructureUtils.setBlock(
                                blockPlacer,
                                Blocks.AIR.getDefaultState(),
                                new BlockPos(x, y, z),
                                this.getCenterPos(),
                                harvestPos,
                                rotation);
                    } else
                        StructureUtils.setBlock(
                            blockPlacer,
                            this.blocks[y][z][x].getStateFromMeta(this.blockData[y][z][x]),
                            new BlockPos(x, y, z),
                            this.getCenterPos(),
                            harvestPos,
                            rotation);
                }
            }
        }

        for (NBTTagCompound tileEntity : this.tileEntities)
            StructureUtils.setTileEntity(
                processData.getWorld(),
                TileEntity.create(processData.getWorld(), tileEntity),
                this.getCenterPos(),
                harvestPos,
                rotation);

        for (NBTTagCompound entity : this.entities)
            StructureUtils.setEntity(
                processData.getWorld(),
                EntityList.createEntityFromNBT(entity, processData.getWorld()),
                this.getCenterPos(),
                harvestPos,
                rotation);

        if (this.blockUpdate) blockPlacer.update();
        this.processOverlay(processData);
    }

    @Override
    public void readFromFile() {
        NBTTagCompound nbtTagCompound = null;
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream(new GZIPInputStream(this.fileStream));
            nbtTagCompound = CompressedStreamTools.read(dataInputStream);
            dataInputStream.close();
        } catch (Exception e) {
            System.err.println("Lucky Block: Error loading structure '" + this.getId() + "'");
            e.printStackTrace();
        }

        // In schematics, length is z and width is x. Here it is reversed.
        this.length = nbtTagCompound.getShort("Width");
        this.width = nbtTagCompound.getShort("Length");
        this.height = nbtTagCompound.getShort("Height");

        int size = this.length * this.width * this.height;
        if (size > STRUCTURE_BLOCK_LIMIT) {
            System.err.println(
                "Lucky Block: Error loading structure. The structure '"
                    + this.getId()
                    + "' ("
                    + size
                    + " blocks) exceeds the "
                    + STRUCTURE_BLOCK_LIMIT
                    + " block limit");
            return;
        }

        this.blocks = new Block[this.height][this.width][this.length];
        this.blockData = new int[this.height][this.width][this.length];

        byte[] blockIdsByte = nbtTagCompound.getByteArray("Blocks");
        byte[] blockDataByte = nbtTagCompound.getByteArray("Data");
        int x = 1, y = 1, z = 1;
        for (int i = 0; i < blockIdsByte.length; i++) {
            int blockId = (short) (blockIdsByte[i] & 0xFF);
            this.blocks[y - 1][z - 1][x - 1] = Block.getBlockById(blockId);
            this.blockData[y - 1][z - 1][x - 1] = blockDataByte[i];
            x++;
            if (x > this.length) {
                x = 1;
                z++;
            }
            if (z > this.width) {
                z = 1;
                y++;
            }
        }

        NBTTagList entityList = nbtTagCompound.getTagList("Entities", 10);
        this.entities = new NBTTagCompound[entityList.tagCount()];
        for (int i = 0; i < entityList.tagCount(); i++)
            this.entities[i] = entityList.getCompoundTagAt(i);

        NBTTagList tileEntityList = nbtTagCompound.getTagList("TileEntities", 10);
        this.tileEntities = new NBTTagCompound[tileEntityList.tagCount()];
        for (int i = 0; i < tileEntityList.tagCount(); i++)
            this.tileEntities[i] = tileEntityList.getCompoundTagAt(i);

        this.initCenterPos();
    }
}
