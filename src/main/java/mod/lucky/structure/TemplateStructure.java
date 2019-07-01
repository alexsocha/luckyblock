package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.ITemplateProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class TemplateStructure extends Structure {
    private Template template;

    private ITemplateProcessor createProcessor() {
        BlockMode blockMode = this.blockMode;
        return new ITemplateProcessor() {
            @Override
            public Template.BlockInfo processBlock(
                IBlockReader worldIn, BlockPos pos, Template.BlockInfo blockInfo) {

                IBlockState blockState = StructureUtils.applyBlockMode(
                    blockMode, blockInfo.blockState);
                if (blockState == null) blockState = worldIn.getBlockState(pos);

                return new Template.BlockInfo(blockInfo.pos,
                    blockState, blockInfo.tileentityData);

            }
        };
    }

    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        BlockPlacer blockPlacer = new BlockPlacer(processData.getWorld());

        Rotation rotation = StructureUtils.parseRotation(drop.getPropertyInt("rotation"));

        PlacementSettings placementSettings = new PlacementSettings()
            .setRotation(rotation)
            .setCenterOffset(new BlockPos(this.centerPos))
            .setIgnoreEntities(false)
            .setChunk(null)
            .setReplacedBlock(null)
            .setIgnoreStructureBlock(true);

        BlockPos adjustedPos = drop.getBlockPos().subtract(new BlockPos(this.centerPos));

        ITemplateProcessor processor = this.createProcessor();
        this.template.getDataBlocks(adjustedPos, placementSettings);
        this.template.addBlocksToWorld(processData.getWorld(), adjustedPos,
            processor, placementSettings, 2);

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
            Lucky.error(e, "Error loading structure '" + this.fileName + "'");
        }

        this.template = new Template();
        if (tag != null) this.template.read(tag);

        this.size = this.template.getSize();
        this.initCenterPos();
    }
}
