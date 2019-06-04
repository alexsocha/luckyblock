package mod.lucky.drop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.value.DropStringUtils;
import mod.lucky.drop.value.DropValue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ForgeRegistries;

public class DropSingle extends DropBase {
    private static String[] multiPosProperties = {"pos", "pos2", "posOffset"};

    private HashMap<String, DropValue> properties;
    private boolean needsInitialize = false;

    public DropSingle() {
        this.properties = new HashMap<String, DropValue>();
    }

    public HashMap<String, DropValue> getProperties() { return this.properties; }

    public DropValue getRawProperty(String name) {
        name = processName(name);
        return this.properties.get(name);
    }

    public Object getProperty(String name) {
        name = processName(name);
        DropValue property = this.properties.get(name);
        if (property == null) {
            return this.getDefaultPropertyValue(name);
        } else return property.getValue();
    }

    public Integer getPropertyInt(String name) {
        if (this.getProperty(name) instanceof Float) return ((Float) this.getProperty(name)).intValue();
        return (Integer) this.getProperty(name);
    }

    public String getPropertyString(String name) {
        return (String) this.getProperty(name);
    }

    public Boolean getPropertyBoolean(String name) {
        return (Boolean) this.getProperty(name);
    }

    public Float getPropertyFloat(String name) {
        if (this.getProperty(name) instanceof Integer)
            return ((Integer) this.getProperty(name)).floatValue();
        return (Float) this.getProperty(name);
    }

    public NBTTagCompound getPropertyNBT(String name) {
        return (NBTTagCompound) this.getProperty(name);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.getVecPos());
    }

    public Vec3d getVecPos() {
        return new Vec3d(
            this.getPropertyFloat("posX"),
            this.getPropertyFloat("posY"),
            this.getPropertyFloat("posZ"));
    }

    public void setBlockPos(BlockPos pos) {
        this.setProperty("posX", pos.getX());
        this.setProperty("posY", pos.getY());
        this.setProperty("posZ", pos.getZ());
    }

    public void setVecPos(Vec3d pos) {
        this.setProperty("posX", pos.x);
        this.setProperty("posY", pos.y);
        this.setProperty("posZ", pos.z);
    }

    public IBlockState getBlockState() {
        String blockID = this.getPropertyString("ID");
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));
        if (block != null) return block.getDefaultState();
        else return null;
    }

    public void setProperty(String name, Object value) {
        name = processName(name);
        this.properties.put(name, new DropValue(value));
    }

    public void setOverrideProperty(String name, Object value) {
        if (!this.hasProperty(name)) this.setProperty(name, value);
    }

    public DropValue setRawProperty(String name, String value) {
        name = processName(name);
        try {
            DropValue property = new DropValue(value, this.getDefaultPropertyType(name));
            if (property.needsInitialize()) this.needsInitialize = true;
            this.properties.put(name, property);
            return property;
        } catch (Exception e) {
            Lucky.LOGGER.error("Error loading property: " + name + "=" + value);
            e.printStackTrace();
        }
        return null;
    }

    public void setOverrideRawProperty(String name, String value) {
        if (!this.hasProperty(name)) this.setRawProperty(name, value);
    }

    public boolean hasProperty(String name) {
        name = processName(name);
        return this.properties.get(name) != null;
    }

    public Object defaultCast(String name, Object object) {
        name = processName(name);
        Class type = this.getDefaultPropertyType(name);
        if (object instanceof Integer && type == Float.class)
            return (float) ((Integer) object).intValue();
        if (object instanceof Float && type == Integer.class)
            return (int) ((Float) object).floatValue();
        else return type.cast(object);
    }

    public boolean needsInitialize() {
        return this.needsInitialize;
    }

    @Override
    public DropSingle initialize(DropProcessData processData) {
        DropSingle properties = this.copy();

        if (!properties.hasProperty("pos")) {
            properties.setOverrideProperty(
                "posX", properties.defaultCast("posX", (float) processData.getHarvestPos().x));
            properties.setOverrideProperty(
                "posY", properties.defaultCast("posY", (float) processData.getHarvestPos().y));
            properties.setOverrideProperty(
                "posZ", properties.defaultCast("posZ", (float) processData.getHarvestPos().z));
        }

        for (String posType : multiPosProperties) {
            if (properties.hasProperty(posType)) {
                properties.getRawProperty(posType).initialize(processData);
                String[] pos =
                    properties
                        .getPropertyString(posType)
                        .substring(1, properties.getPropertyString(posType).length() - 1)
                        .split(",");
                properties.setOverrideRawProperty(posType + "X", pos[0]);
                properties.setOverrideRawProperty(posType + "Y", pos[1]);
                properties.setOverrideRawProperty(posType + "Z", pos[2]);
            }
        }
        if (properties.hasProperty("size")) {
            properties.getRawProperty("size").initialize(processData);
            String[] size =
                properties
                    .getPropertyString("size")
                    .substring(1, properties.getPropertyString("size").length() - 1)
                    .split(",");
            properties.setOverrideRawProperty("length", size[0]);
            properties.setOverrideRawProperty("height", size[1]);
            properties.setOverrideRawProperty("width", size[2]);
        }

        if (properties.needsInitialize) {
            Iterator iterator = properties.properties.entrySet().iterator();
            while (iterator.hasNext()) {
                ((DropValue) ((Map.Entry) iterator.next()).getValue()).initialize(processData);
            }
        }

        if (properties.hasProperty("posOffsetX"))
            properties
                .getRawProperty("posX")
                .setValue(
                    properties.defaultCast(
                        "posX",
                        properties.getPropertyFloat("posX") + properties.getPropertyFloat("posOffsetX")));
        if (properties.hasProperty("posOffsetY"))
            properties
                .getRawProperty("posY")
                .setValue(
                    properties.defaultCast(
                        "posY",
                        properties.getPropertyFloat("posY") + properties.getPropertyFloat("posOffsetY")));
        if (properties.hasProperty("posOffsetZ"))
            properties
                .getRawProperty("posZ")
                .setValue(
                    properties.defaultCast(
                        "posZ",
                        properties.getPropertyFloat("posZ") + properties.getPropertyFloat("posOffsetZ")));

        for (String posType : multiPosProperties) {
            if (!properties.hasProperty(posType)
                && (properties.hasProperty(posType + "X")
                || properties.hasProperty(posType + "Y")
                || properties.hasProperty(posType + "Z"))) {
                properties.setProperty(
                    posType,
                    "("
                        + properties.getProperty(posType + "X").toString()
                        + ","
                        + properties.getProperty(posType + "Y").toString()
                        + ","
                        + properties.getProperty(posType + "Z").toString()
                        + ")");
            }
        }

        return properties;
    }

    @Override
    public void readFromString(String string) {
        String[] properties = DropStringUtils.splitBracketString(string, ',');
        for (int parse = 0; parse < 2; parse++) {
            for (String property : properties) {
                String name =
                    property.substring(0, property.indexOf("=")).trim().toLowerCase(Locale.ENGLISH);
                String value = property.substring(property.indexOf("=") + 1).trim();
                // type is loaded first
                if (parse == 0 && name.toLowerCase(Locale.ENGLISH).equals("type"))
                    this.setRawProperty(name, value);
                else if (parse == 1 && !name.toLowerCase(Locale.ENGLISH).equals("type"))
                    this.setRawProperty(name, value);
            }
        }

        if (this.needsInitialize && !this.hasProperty("reinitialize"))
            this.setProperty("reinitialize", true);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagCompound propertiesTag = tagCompound.getCompound("properties");
        for (Object key : propertiesTag.keySet()) {
            DropValue dropValue = new DropValue(null);
            dropValue.readFromNBT((NBTTagCompound) propertiesTag.getTag((String) key));
            this.properties.put((String) key, dropValue);
        }

        this.needsInitialize = tagCompound.getBoolean("needsInitialize");
    }

    @Override
    public String writeToString() {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound mainTag = new NBTTagCompound();

        NBTTagCompound propertiesTag = new NBTTagCompound();
        for (String key : this.properties.keySet())
            propertiesTag.setTag(key, this.properties.get(key).writeToNBT());

        mainTag.setTag("properties", propertiesTag);
        mainTag.setBoolean("needsInitialize", this.needsInitialize);

        return mainTag;
    }

    private Class getDefaultPropertyType(String name) {
        name = processName(name);
        if (this.hasProperty("type")) {
            String type = this.getPropertyString("type");
            if (defaultValueTypes.get(type) != null && defaultValueTypes.get(type).containsKey(name))
                return defaultValueTypes.get(type).get(name);
        }
        return defaultValueTypes.get("all").get(name);
    }

    private Object getDefaultPropertyValue(String name) {
        name = processName(name);
        if (this.hasProperty("type")) {
            String type = this.getPropertyString("type");
            if (defaultValues.get(type) != null && defaultValues.get(type).containsKey(name))
                return defaultValues.get(type).get(name);
        }
        return defaultValues.get("all").get(name);
    }

    public DropSingle copy() {
        DropSingle dropSingle = new DropSingle();
        for (String key : this.properties.keySet())
            dropSingle.properties.put(key, this.properties.get(key).copy());

        dropSingle.needsInitialize = this.needsInitialize;
        return dropSingle;
    }

    private static HashMap<String, HashMap<String, Class>> defaultValueTypes =
        new HashMap<String, HashMap<String, Class>>();
    private static HashMap<String, HashMap<String, Object>> defaultValues =
        new HashMap<String, HashMap<String, Object>>();
    private static HashMap<String, String> replaceProperties = new HashMap<String, String>();

    public static void setDefaultProperty(
        String type, String name, Class valueType, Object defaultValue) {
        name = processName(name);
        if (defaultValueTypes.get(type) == null)
            defaultValueTypes.put(type, new HashMap<String, Class>());
        defaultValueTypes.get(type).put(name, valueType);

        if (defaultValues.get(type) == null) defaultValues.put(type, new HashMap<String, Object>());
        defaultValues.get(type).put(name, defaultValue);

        if (!type.equals("all")) setDefaultProperty("all", name, valueType, defaultValue);
    }

    public static void setReplaceProperty(String original, String replace) {
        replaceProperties.put(
            original.toLowerCase(Locale.ENGLISH), replace.toLowerCase(Locale.ENGLISH));
    }

    private static String processName(String name) {
        name = name.toLowerCase(Locale.ENGLISH);
        if (replaceProperties.get(name) != null) name = replaceProperties.get(name);
        return name;
    }

    @Override
    public String toString() {
        String string = "";
        for (String key : this.properties.keySet())
            string += (key + "=" + this.properties.get(key).toString() + ",");
        return string.substring(0, string.length() - 1);
    }
}
