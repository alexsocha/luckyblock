package mod.lucky.structure;

import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
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
                IWorldReader worldIn,
                BlockPos structPos,
                Template.BlockInfo oldBlockInfo,
                Template.BlockInfo newBlockInfo,
                PlacementSettings settings) {

                BlockState blockState = StructureUtils.applyBlockMode(
                    blockMode, newBlockInfo.state);
                if (blockState == null)
                    blockState = worldIn.getBlockState(newBlockInfo.pos);

                return new Template.BlockInfo(newBlockInfo.pos,
                    blockState, newBlockInfo.nbt);
            }

            protected IStructureProcessorType getType() {
                return IStructureProcessorType.BLOCK_IGNORE;
            }

            protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
                return new Dynamic<>(ops, ops.emptyMap());
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

        this.template.addBlocksToWorld(processData.getRawWorld(),
            adjustedPos, placementSettings, 2);

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
