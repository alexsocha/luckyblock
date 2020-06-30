package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class TemplateStructure extends Structure {
    private Template template;

    private StructureProcessor createProcessor() {
        BlockMode blockMode = this.blockMode;
        return new StructureProcessor() {
            @Override
            public Template.BlockInfo process(
                IWorldReader world,
                BlockPos oldPos,
                BlockPos newPos,
                Template.BlockInfo oldBlockInfo,
                Template.BlockInfo newBlockInfo,
                PlacementSettings settings,
                Template template) {

                BlockState blockState = StructureUtils.applyBlockMode(
                    blockMode, newBlockInfo.state);
                if (blockState == null)
                    blockState = world.getBlockState(newBlockInfo.pos);

                return new Template.BlockInfo(newBlockInfo.pos,
                    blockState, newBlockInfo.nbt);
            }

            protected IStructureProcessorType getType() {
                return IStructureProcessorType.BLOCK_IGNORE;
            }
        };
    }

    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        BlockPlacer blockPlacer = new BlockPlacer(processData.getRawWorld());

        Rotation rotation = StructureUtils.parseRotation(drop.getPropertyInt("rotation"));

        PlacementSettings placementSettings = new PlacementSettings()
            .setRotation(rotation)
            .setCenterOffset(new BlockPos(this.centerPos))
            .setIgnoreEntities(false)
            .setChunk(null)
            .addProcessor(this.createProcessor());

        BlockPos adjustedPos = drop.getBlockPos().subtract(new BlockPos(this.centerPos));

        this.template.func_237144_a_(processData.getRawWorld(),
            adjustedPos, placementSettings, new Random());

        if (this.blockUpdate) blockPlacer.update();
        this.processOverlay(processData);
    }

    @Override
    public void readFromFile() {
        CompoundNBT tag = null;
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
