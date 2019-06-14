package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.ITemplateProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;

public class TemplateStructure extends Structure {
    private Template template;

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
        int rotationInt = drop.getPropertyInt("rotation");
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
        this.template.addBlocksToWorld(processData.getWorld(), drop.getBlockPos(),
            processor, placementSettings, 0);

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
            Lucky.error(e, "Error loading structure '" + this.id + "'");
        }

        this.template = new Template();
        if (tag != null) this.template.read(tag);

        this.size = this.template.getSize();
        this.initCenterPos();
    }
}
