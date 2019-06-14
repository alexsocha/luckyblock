package mod.lucky.structure;

import java.io.InputStream;
import java.util.HashMap;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.value.DropStringUtils;
import mod.lucky.drop.value.ValueParser;
import mod.lucky.resources.ResourceStructureFile;
import mod.lucky.resources.loader.BaseLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Structure {
    public enum BlockMode {
        REPLACE("replace"),
        OVERLAY("overlay"),
        AIR("air");

        private String name;
        private static HashMap<String, BlockMode> lookup = new HashMap<>();

        BlockMode(String name) { this.name = name; }

        static {
            for (BlockMode mode : BlockMode.values())
                lookup.put(mode.toString(), mode);
        }

        public String toString() { return this.name; }
        public static BlockMode fromString(String s) { return lookup.get(s); }
    }

    public static final int STRUCTURE_BLOCK_LIMIT = 100000;

    protected BlockPos size;

    protected String fileName;
    private BaseLoader loader;

    protected String overlayStruct;
    public String id;
    protected BlockMode blockMode;
    protected boolean blockUpdate;

    protected boolean[] explicitCenter = { false, false, false };
    public Vec3d centerPos = new Vec3d(0, 0, 0);

    public Structure() {
        this.blockMode = BlockMode.REPLACE;
        this.blockUpdate = true;
    }

    public void readProperties(String properties, BaseLoader loader) {
        this.loader = loader;

        double centerX = 0; double centerY = 0; double centerZ = 0;

        String[] splitProperties = properties.split(",");
        for (String property : splitProperties) {
            String[] splitProperty = property.split("=");
            String propertyName = splitProperty[0];
            String propertyValue = splitProperty[1];

            if (propertyName.equalsIgnoreCase("ID")) this.id = ValueParser.getString(propertyValue);
            if (propertyName.equalsIgnoreCase("overlayStruct"))
                this.overlayStruct = ValueParser.getString(propertyValue);
            if (propertyName.equalsIgnoreCase("file")) {
                this.fileName = ValueParser.getString(propertyValue);
            }
            if (propertyName.equalsIgnoreCase("centerX")) {
                this.explicitCenter[0] = true;
                centerX = ValueParser.getFloat(propertyValue);
                if (!DropStringUtils.isGenericFloat(propertyValue)) centerX += 0.5F;
            }
            if (propertyName.equalsIgnoreCase("centerY")) {
                this.explicitCenter[1] = true;
                centerY = ValueParser.getFloat(propertyValue);
            }
            if (propertyName.equalsIgnoreCase("centerZ")) {
                this.explicitCenter[2] = true;
                centerZ = ValueParser.getFloat(propertyValue);
                if (!DropStringUtils.isGenericFloat(propertyValue)) centerZ += 0.5F;
            }
            if (propertyName.equalsIgnoreCase("blockMode"))
                this.blockMode = BlockMode.fromString(ValueParser.getString(propertyValue));
            if (propertyName.equalsIgnoreCase("blockUpdate"))
                this.blockUpdate = ValueParser.getBoolean(propertyValue);
        }
        this.centerPos = new Vec3d(centerX, centerY, centerZ);
    }

    public Structure newTypeInstance() {
        if (this.fileName.endsWith(".luckystruct")) {
            return new LuckyStructure().copyProperties(this);
        } else {
            return new TemplateStructure().copyProperties(this);
        }
    }

    public void readFromFile() {
    }

    public void process(DropProcessData processData) {
    }

    protected void processOverlay(DropProcessData processData) {
        if (this.overlayStruct != null) {
            DropSingle drop = processData.getDropSingle();

            String oldId = drop.getPropertyString("ID");
            String oldBlockMode = drop.getPropertyString("applyBlockMode");

            drop.setProperty("ID", this.overlayStruct);
            drop.setProperty("applyBlockMode", "overlay");
            DropFunction.getDropFunction(drop).process(processData);
            drop.setProperty("ID", oldId);
            drop.setProperty("applyBlockMode", oldBlockMode);
        }
    }

    protected void initCenterPos() {
        double centerX = !this.explicitCenter[0]
            ? (int) (this.size.getX() / 2.0) + 0.5 : this.centerPos.x;
        double centerZ = !this.explicitCenter[2]
            ? (int) (this.size.getZ() / 2.0) + 0.5 : this.centerPos.z;
        double centerY = !this.explicitCenter[1] ? 0 : this.centerPos.y;

        this.centerPos = new Vec3d(centerX, centerY, centerZ);
    }

    public InputStream openFileStream() {
        return loader.getResourceStream(new ResourceStructureFile(this.fileName));
    }

    public Structure copyProperties(Structure structure) {
        this.loader = structure.loader;
        this.fileName = structure.fileName;
        this.id = structure.id;
        this.overlayStruct = structure.overlayStruct;
        this.centerPos = structure.centerPos;
        this.blockMode = structure.blockMode;
        this.blockUpdate = structure.blockUpdate;
        this.size = structure.size;
        return this;
    }
}
