package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemIntIDToString;
import net.minecraft.util.datafix.fixes.ItemStackDataFlattening;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

public class LegacySchematicStructure extends Structure {
    // Blocks stored [y][z][x]
    private BlockState[][][] blocks;
    private int[][][] blockData;

    private CompoundNBT[] entities;
    private CompoundNBT[] tileEntities;

    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        Vec3d harvestPos = drop.getVecPos();
        int rotation = drop.getPropertyInt("rotation");
        BlockPlacer blockPlacer = new BlockPlacer(processData.getRawWorld());

        for (int y = 0; y < this.size.getY(); y++) {
            for (int z = 0; z < this.size.getZ(); z++) {
                for (int x = 0; x < this.size.getX(); x++) {
                    BlockState blockStateInit = this.blocks[y][z][x];
                    BlockPos blockPos = new BlockPos(x, y, z);

                    BlockState blockState = StructureUtils.applyBlockMode(
                        this.blockMode, blockStateInit);
                    if (blockState != null) {
                        StructureUtils.setBlock(blockPlacer, blockState,
                            blockPos, this.centerPos,
                            harvestPos, rotation);

                        if (this.blockMode != BlockMode.AIR && drop.hasProperty("tileEntity")) {
                            StructureUtils.setTileEntity(
                                processData.getWorld(),
                                drop.getPropertyNBT("tileEntity"),
                                blockPos, this.centerPos,
                                harvestPos, rotation);
                        }
                    }
                }
            }
        }

        for (CompoundNBT entityNbt : this.entities)
            StructureUtils.setEntity(
                processData.getWorld(),
                EntityType.func_220335_a(entityNbt, processData.getWorld(), e -> e),
                this.centerPos,
                harvestPos,
                rotation);

        if (this.blockUpdate) blockPlacer.update();
        this.processOverlay(processData);
    }

    @Override
    public void readFromFile() {
        CompoundNBT nbt = null;
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream(new GZIPInputStream(this.openFileStream()));
            nbt = CompressedStreamTools.read(dataInputStream);
            dataInputStream.close();
        } catch (Exception e) {
            Lucky.error(e, "Error loading legacy schematic structure '" + this.id + "'");
            e.printStackTrace();
        }

        // In schematics, length is z and width is x. Here it is reversed.
        this.size = new BlockPos(
            nbt.getShort("Width"), nbt.getShort("Height"), nbt.getShort("Length"));

        this.blocks = new BlockState[this.size.getY()][this.size.getZ()][this.size.getX()];

        byte[] blockIdsByte = nbt.getByteArray("Blocks");
        byte[] blockDataByte = nbt.getByteArray("Data");
        int x = 1, y = 1, z = 1;
        for (int i = 0; i < blockIdsByte.length; i++) {
            int blockId = (short) (blockIdsByte[i] & 0xFF);
            byte blockData = blockDataByte[i];

            String legacyName = ItemIntIDToString.getItem(blockId);
            String name = ItemStackDataFlattening.updateItem(legacyName, blockData);
            if (legacyName.equals("minecraft:air") && blockId > 0) name = "minecraft:stone";
            if (name == null) name = legacyName;

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
            this.blocks[y - 1][z - 1][x - 1] = block.getDefaultState();

            x++;
            if (x > this.size.getX()) {
                x = 1;
                z++;
            }
            if (z > this.size.getZ()) {
                z = 1;
                y++;
            }
        }

        ListNBT entityList = nbt.getList("Entities", Constants.NBT.TAG_COMPOUND);
        this.entities = new CompoundNBT[entityList.size()];
        for (int i = 0; i < entityList.size(); i++)
            this.entities[i] = entityList.getCompound(i);

        ListNBT tileEntityList = nbt.getList("TileEntities", Constants.NBT.TAG_COMPOUND);
        this.tileEntities = new CompoundNBT[tileEntityList.size()];
        for (int i = 0; i < tileEntityList.size(); i++)
            this.tileEntities[i] = tileEntityList.getCompound(i);

        this.initCenterPos();
    }
}
