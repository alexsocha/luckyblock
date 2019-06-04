package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.ITemplateProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;

public class SchematicStructure extends Structure {
    private Template template;
    // Blocks stored [y][z][x]
    private Block[][][] blocks;
    private int[][][] blockData;

    private NBTTagCompound[] entities;
    private NBTTagCompound[] tileEntities;

    private ITemplateProcessor createProcessor() {
        BlockMode blockMode = this.blockMode;
        return new ITemplateProcessor() {
            @Nullable
            @Override
            public Template.BlockInfo processBlock(
                IBlockReader worldIn, BlockPos pos, Template.BlockInfo blockInfo) {

                IBlockState blockState = StructureUtils.applyBlockMode(
                    blockMode, blockInfo.blockState);
                return new Template.BlockInfo(blockInfo.pos,
                    blockState, blockInfo.tileentityData);

            }
        };
    }

    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        Vec3d harvestPos = drop.getVecPos();
        int rotation = drop.getPropertyInt("rotation");
        BlockPlacer blockPlacer = new BlockPlacer(processData.getWorld());

        Rotation rotation = StructureUtils.parseRotation(drop.getPropertyInt("rotation"));
        boolean ignoreEntities = drop.getPropertyBoolean("ignoreEntities");

        PlacementSettings placementSettings = new PlacementSettings()
            .setRotation(rotation)
            .setIgnoreEntities(ignoreEntities)
            .setChunk(null)
            .setReplacedBlock(null)
            .setIgnoreStructureBlock(true);

        ITemplateProcessor processor = this.createProcessor();
        template.addBlocksToWorldChunk(this.world, blockpos1, placementsettings);

        if (this.blockUpdate) blockPlacer.update();
        this.processOverlay(processData);
    }

    @Override
    public void readFromFile() {
        NBTTagCompound tag = null;
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream(
                new GZIPInputStream(this.openFileStream()));
            tag = CompressedStreamTools.read(dataInputStream);
            dataInputStream.close();
        } catch (Exception e) {
            Lucky.LOGGER.error("Error loading structure '" + this.getId() + "'");
        }

        this.template = new Template();
        if (tag != null) this.template.read(tag);

        this.sizeX = this.template.getSize().getX();
        this.sizeY = this.template.getSize().getY();
        this.sizeZ = this.template.getSize().getZ();
        this.initCenterPos();
    }
}
