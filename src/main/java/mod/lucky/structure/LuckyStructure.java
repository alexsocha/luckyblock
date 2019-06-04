package mod.lucky.structure;

import java.io.InputStreamReader;
import java.util.ArrayList;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.value.DropStringUtils;
import mod.lucky.drop.value.ValueParser;
import mod.lucky.structure.rotation.Rotations;
import mod.lucky.util.LuckyReader;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LuckyStructure extends Structure {
    private ArrayList<DropSingle> blocks;
    private ArrayList<DropSingle> entities;

    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        Vec3d harvestPos = drop.getVecPos();
        int rotation = drop.getPropertyInt("rotation");
        BlockPlacer blockPlacer = new BlockPlacer(processData.getWorld());

        if (!this.blockMode.equals("overlay") && !this.blockMode.equals("air")) {
            for (int x = 0; x < this.length; x++) {
                for (int y = 0; y < this.height; y++) {
                    for (int z = 0; z < this.width; z++) {
                        StructureUtils.setBlock(
                            blockPlacer,
                            Blocks.AIR.getDefaultState(),
                            new BlockPos(x, y, z),
                            this.getCenterPos(),
                            harvestPos,
                            rotation);
                    }
                }
            }
        }

        DropProcessData blockProcessData = processData.copy();
        blockProcessData.setProcessType(DropProcessData.EnumProcessType.LUCKY_STRUCT);
        for (DropSingle blockProperties : this.blocks) {
            blockProperties = blockProperties.initialize(blockProcessData);
            blockProcessData.setDropSingle(blockProperties);
            if (this.blockMode.equals("air")) {
                if (blockProperties.getBlockState().getBlock() != Blocks.AIR)
                    StructureUtils.setBlock(
                        blockPlacer,
                        Blocks.AIR.getDefaultState(),
                        blockProperties.getBlockPos(),
                        this.getCenterPos(),
                        harvestPos,
                        rotation);
            } else {
                StructureUtils.setBlock(
                    blockPlacer,
                    blockProperties.getBlockState(),
                    blockProperties.getBlockPos(),
                    this.getCenterPos(),
                    harvestPos,
                    rotation);
                if (blockProperties.getPropertyNBT("tileEntity") != null)
                    StructureUtils.setTileEntity(
                        blockProcessData.getWorld(),
                        blockProperties.getPropertyNBT("tileEntity"),
                        blockProperties.getBlockPos(),
                        this.getCenterPos(),
                        harvestPos,
                        rotation);
            }
        }

        DropProcessData entityProcessData = processData.copy();
        entityProcessData.setProcessType(DropProcessData.EnumProcessType.LUCKY_STRUCT);
        for (DropSingle entityProperties : this.entities) {
            entityProperties = entityProperties.initialize(entityProcessData);
            entityProcessData.setDropSingle(entityProperties);
            Vec3d originalPos = entityProperties.getVecPos();
            if (entityProperties.getPropertyNBT("NBTTag") != null)
                Rotations.rotateEntity(
                    entityProperties.getPropertyNBT("NBTTag"),
                    harvestPos.add(this.getCenterPos()),
                    rotation);
            entityProperties.setVecPos(
                StructureUtils.getWorldPos(originalPos, this.getCenterPos(), harvestPos, rotation));
            entityProcessData.setDropSingle(entityProperties);
            DropFunction.getDropFunction("entity").process(entityProcessData);
            entityProperties.setVecPos(originalPos);
        }

        if (this.blockUpdate) blockPlacer.update();
        this.processOverlay(processData);
    }

    @Override
    public void readFromFile() {
        try {
            LuckyReader reader = new LuckyReader(new InputStreamReader(this.fileStream));

            String section = "";
            String curLine;

            this.blocks = new ArrayList<DropSingle>();
            this.entities = new ArrayList<DropSingle>();

            while ((curLine = reader.readLine()) != null) {
                if (curLine.startsWith(">")) {
                    section = curLine;
                    continue;
                }

                if (section.equals(">properties")) {
                    String name = curLine.substring(0, curLine.indexOf('='));
                    String value = curLine.substring(curLine.indexOf('=') + 1, curLine.length());

                    if (name.equals("length")) this.length = ValueParser.getInteger(value);
                    if (name.equals("width")) this.width = ValueParser.getInteger(value);
                    if (name.equals("height")) this.height = ValueParser.getInteger(value);

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
                        reader.close();
                        return;
                    }
                }
                if (section.equals(">blocks")) {
                    DropSingle dropSingle = new DropSingle();
                    String[] properties = DropStringUtils.splitBracketString(curLine, ',');

                    dropSingle.setRawProperty("type", "block");
                    dropSingle.setRawProperty("posX", properties[0]);
                    dropSingle.setRawProperty("posY", properties[1]);
                    dropSingle.setRawProperty("posZ", properties[2]);
                    dropSingle.setRawProperty("ID", properties[3]);
                    if (properties.length > 4) dropSingle.setRawProperty("meta", properties[4]);
                    if (properties.length > 5) dropSingle.setRawProperty("tileEntity", properties[5]);
                    this.blocks.add(dropSingle);
                }
                if (section.equals(">entities")) {
                    DropSingle dropSingle = new DropSingle();
                    String[] properties = DropStringUtils.splitBracketString(curLine, ',');

                    dropSingle.setRawProperty("type", "entity");
                    dropSingle.setRawProperty("posX", properties[0]);
                    dropSingle.setRawProperty("posY", properties[1]);
                    dropSingle.setRawProperty("posZ", properties[2]);
                    dropSingle.setRawProperty("ID", properties[3]);
                    if (properties.length > 4) dropSingle.setRawProperty("NBTTag", properties[4]);
                    this.blocks.add(dropSingle);
                }
            }
            reader.close();

            this.initCenterPos();
        } catch (Exception e) {
            System.err.println("Lucky Block: Error loading structure '" + this.getId() + "'");
            e.printStackTrace();
        }
    }
}
