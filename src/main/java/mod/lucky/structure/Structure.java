package mod.lucky.structure;

import java.io.InputStream;
import mod.lucky.drop.DropProperties;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.value.DropStringUtils;
import mod.lucky.drop.value.ValueParser;
import mod.lucky.resources.ResourceStructureFile;
import mod.lucky.resources.loader.BaseLoader;
import net.minecraft.util.math.Vec3d;

public class Structure {
  public static final int STRUCTURE_BLOCK_LIMIT = 100000;

  protected int length;
  protected int height;
  protected int width;

  protected String fileName;
  protected InputStream fileStream;
  protected String overlayStruct;
  protected String id;
  protected String blockMode;
  protected boolean blockUpdate;

  protected Float centerX;
  protected Float centerY;
  protected Float centerZ;
  protected Vec3d centerPos;

  public Structure() {
    this.blockMode = "replace";
    this.blockUpdate = true;
  }

  public void readProperties(String properties, BaseLoader loader) {
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
        this.fileStream = loader.getResourceStream(new ResourceStructureFile(this.fileName));
      }
      if (propertyName.equalsIgnoreCase("centerX")) {
        if (DropStringUtils.isGenericFloat(propertyValue))
          this.centerX = ValueParser.getFloat(propertyValue);
        else
          this.centerX =
              ValueParser.getFloat(propertyValue)
                  + (DropStringUtils.isGenericFloat(propertyValue) ? 0.0F : 0.5F);
      }
      if (propertyName.equalsIgnoreCase("centerY"))
        this.centerY = ValueParser.getFloat(propertyValue);
      if (propertyName.equalsIgnoreCase("centerZ")) {
        if (DropStringUtils.isGenericFloat(propertyValue))
          this.centerZ = ValueParser.getFloat(propertyValue);
        else
          this.centerZ =
              ValueParser.getFloat(propertyValue)
                  + (DropStringUtils.isGenericFloat(propertyValue) ? 0.0F : 0.5F);
      }
      if (propertyName.equalsIgnoreCase("blockMode"))
        this.blockMode = ValueParser.getString(propertyValue);
      if (propertyName.equalsIgnoreCase("blockUpdate"))
        this.blockUpdate = ValueParser.getBoolean(propertyValue);
    }
  }

  public Structure newTypeInstance() {
    if (this.fileName.endsWith(".luckystruct")) {
      return new LuckyStructure().copyProperties(this);
    } else {
      return new SchematicStructure().copyProperties(this);
    }
  }

  public void readFromFile() {}

  public void process(DropProcessData processData) {}

  protected void processOverlay(DropProcessData processData) {
    if (this.overlayStruct != null) {
      DropProperties drop = processData.getDropProperties();

      String oldId = drop.getPropertyString("ID");
      String oldBlockMode = drop.getPropertyString("blockMode");

      drop.setProperty("ID", this.overlayStruct);
      drop.setProperty("blockMode", "overlay");
      DropFunction.getDropFunction(drop).process(processData);
      drop.setProperty("ID", oldId);
      drop.setProperty("blockMode", oldBlockMode);
    }
  }

  public String getId() {
    return this.id;
  }

  public Vec3d getCenterPos() {
    return this.centerPos;
  }

  protected void initCenterPos() {
    int defaultCenterX = (int) (this.length / 2.0F);
    int defaultCenterZ = (int) (this.width / 2.0F);
    if (this.centerX == null) this.centerX = defaultCenterX + 0.5F;
    if (this.centerY == null) this.centerY = 0.0F;
    if (this.centerZ == null) this.centerZ = defaultCenterZ + 0.5F;
    this.centerPos = new Vec3d(this.centerX, this.centerY, this.centerZ);
  }

  public Structure copyProperties(Structure structure) {
    this.fileName = structure.fileName;
    this.fileStream = structure.fileStream;
    this.id = structure.id;
    this.overlayStruct = structure.overlayStruct;
    this.centerX = structure.centerX;
    this.centerY = structure.centerY;
    this.centerZ = structure.centerZ;
    this.centerPos = structure.centerPos;
    this.blockMode = structure.blockMode;
    this.blockUpdate = structure.blockUpdate;
    this.length = structure.length;
    this.height = structure.height;
    this.width = structure.width;
    return this;
  }
}
