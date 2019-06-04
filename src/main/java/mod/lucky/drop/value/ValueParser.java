package mod.lucky.drop.value;

import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Arrays;

import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringEscapeUtils;

public class ValueParser {
    public static HashBiMap<String, Class> classTypeToString;

    static {
        classTypeToString = HashBiMap.create();
        classTypeToString.put("string", String.class);
        classTypeToString.put("int", Integer.class);
        classTypeToString.put("boolean", Boolean.class);
        classTypeToString.put("float", Float.class);
        classTypeToString.put("nbt", NBTTagCompound.class);
    }

    public static Object getObject(String string, Class objectType, DropProcessData processData)
        throws Exception {
        if (objectType == String.class) return getString(string, processData);
        if (objectType == Integer.class) return getInteger(string, processData);
        if (objectType == Boolean.class) return getBoolean(string, processData);
        if (objectType == Float.class) return getFloat(string, processData);
        if (objectType == Double.class) return getDouble(string, processData);
        if (objectType == Short.class) return getShort(string, processData);
        if (objectType == Byte.class) return getByte(string, processData);
        if (objectType == NBTTagCompound.class) return getNBTTag(string, processData);
        return null;
    }

    public static INBTBase getNBTBaseFromValue(Object value) {
        if (value.getClass() == String.class) return new NBTTagString((String) value);
        if (value.getClass() == Integer.class) return new NBTTagInt((Integer) value);
        if (value.getClass() == Boolean.class)
            return new NBTTagByte((byte) ((Boolean) value == true ? 1 : 0));
        if (value.getClass() == Float.class) return new NBTTagFloat((Float) value);
        if (value.getClass() == NBTTagCompound.class) return (NBTTagCompound) value;
        return null;
    }

    public static Object getValueFromNBTBase(INBTBase nbtBase) {
        if (nbtBase.getClass() == NBTTagString.class) return ((NBTTagString) nbtBase).getString();
        if (nbtBase.getClass() == NBTTagInt.class) return ((NBTTagInt) nbtBase).getInt();
        if (nbtBase.getClass() == NBTTagByte.class)
            return ((NBTTagByte) nbtBase).getByte() == 1 ? true : false;
        if (nbtBase.getClass() == NBTTagFloat.class) return ((NBTTagFloat) nbtBase).getFloat();
        if (nbtBase.getClass() == NBTTagCompound.class) return nbtBase;
        return null;
    }

    public static String getString(String string) throws NumberFormatException {
        return getString(string, null);
    }

    public static String getString(String string, DropProcessData processData) {
        string = HashVariables.processString(string, processData);
        string = StringEscapeUtils.unescapeJava(string);
        if (string.startsWith("\"") && string.endsWith("\""))
            string = string.substring(1, string.length() - 1);
        return string;
    }

    public static Double calculateNum(String string) throws NumberFormatException {
        string = DropStringUtils.removeNumSuffix(string);
        string = string.trim();
        double result = 0;

        String prefixSign = "";
        if (string.startsWith("-") || string.startsWith("+")) {
            prefixSign = string.substring(0, 1);
            string = string.substring(1, string.length());
        }

        char operator =
            string.contains("+")
                ? '+'
                : (string.contains("-")
                ? '-'
                : (string.contains("*") ? '*' : (string.contains("/") ? '/' : 0)));
        if (operator != 0) {
            String[] splitString = DropStringUtils.splitBracketString(string, operator);
            if (splitString.length >= 2) {
                double num1 = Double.valueOf(prefixSign + splitString[0]);
                double num2 = Double.valueOf(splitString[1]);

                switch (operator) {
                    case '+':
                        result = num1 + num2;
                        break;
                    case '-':
                        result = num1 - num2;
                        break;
                    case '*':
                        result = num1 * num2;
                        break;
                    case '/':
                        result = num1 / num2;
                        break;
                }
            }
        } else result = Double.valueOf(prefixSign + string);

        return result;
    }

    public static Integer getInteger(String string) throws NumberFormatException {
        return getInteger(string, null);
    }

    public static Integer getInteger(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        return calculateNum(string).intValue();
    }

    public static Double getDouble(String string) throws NumberFormatException {
        return getDouble(string, null);
    }

    public static Double getDouble(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        return calculateNum(string);
    }

    public static Float getFloat(String string) throws NumberFormatException {
        return getFloat(string, null);
    }

    public static Float getFloat(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        return calculateNum(string).floatValue();
    }

    public static Boolean getBoolean(String string) throws NumberFormatException {
        return getBoolean(string, null);
    }

    public static Boolean getBoolean(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        if (!string.equals("true") && !string.equals("false"))
            throw new NumberFormatException("Lucky Block: Unknown boolean format: " + string);
        return Boolean.valueOf(string);
    }

    public static Short getShort(String string) {
        return getShort(string, null);
    }

    public static Short getShort(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        return calculateNum(string).shortValue();
    }

    public static Byte getByte(String string) throws NumberFormatException {
        return getByte(string, null);
    }

    public static Byte getByte(String string, DropProcessData processData)
        throws NumberFormatException {
        string = HashVariables.processString(string, processData);
        return calculateNum(string).byteValue();
    }

    public static NBTTagCompound getNBTTag(String string) throws Exception {
        return getNBTTag(string, null);
    }

    public static NBTTagCompound getNBTTag(String string, DropProcessData processData)
        throws Exception {
        Object tagBase = getNBTTagValue(string, processData);
        if (tagBase instanceof NBTTagCompound) return (NBTTagCompound) tagBase;
        else throw new Exception();
    }

    public static Object getNBTTagValue(String string, DropProcessData processData) throws Exception {
        return getNBTTagValue(string, processData, null, null);
    }

    private static ArrayList<String> DROP_TAGS =
        new ArrayList<String>(Arrays.asList("drops", "impact"));

    public static Object getNBTTagValue(
        String string, DropProcessData processData, INBTBase parentTag, String parentTagName)
        throws Exception {

        if (string.startsWith("(") && string.endsWith(")")) {
            String[] tagContents =
                DropStringUtils.splitBracketString(string.substring(1, string.length() - 1), ',');
            NBTTagCompound tagCompound = new NBTTagCompound();
            for (String tag : tagContents) {
                if (tag == null || tag.equals("")) continue;
                String tagName = tag.substring(0, tag.indexOf("="));
                String tagValue = tag.substring(tag.indexOf("=") + 1);
                setNBTTagValue(
                    tagCompound, tagName, getNBTTagValue(tagValue, processData, tagCompound, tagName));
            }
            return tagCompound;
        }
        if (string.startsWith("[") && string.endsWith("]")) {
            String[] tagContents =
                DropStringUtils.splitBracketString(string.substring(1, string.length() - 1), ',');
            NBTTagList tagList = new NBTTagList();
            for (String tag : tagContents) {
                if (tag == null || tag.equals("")) continue;
                setNBTTagValue(tagList, null, getNBTTagValue(tag, processData, tagList, parentTagName));
            }
            return tagList;
        }

        if (string.startsWith("\"") && string.endsWith("\"")) {
            if (parentTag != null
                && parentTag instanceof NBTTagList
                && parentTagName != null
                && DROP_TAGS.contains(parentTagName.toLowerCase())) {
                // Auto cancel hash
                string = HashVariables.autoCancelHash(string);
            }
            return getString(string, processData);
        }
        if (string.startsWith("#")) {
            INBTBase tagBase = CustomNBTTags.getNBTTagFromString(string, processData);
            if (tagBase != null) return tagBase;
        }

        string = getString(string, processData);
        try {
            if (string.endsWith("f") || string.endsWith("F"))
                return getFloat(DropStringUtils.removeNumSuffix(string), processData);
            if (string.endsWith("d") || string.endsWith("D") || DropStringUtils.hasDecimalPoint(string))
                return getDouble(DropStringUtils.removeNumSuffix(string), processData);
            if (string.endsWith("s") || string.endsWith("S"))
                return getShort(DropStringUtils.removeNumSuffix(string), processData);
            if (string.endsWith("b") || string.endsWith("B"))
                return getByte(DropStringUtils.removeNumSuffix(string), processData);
            if (string.equals("true") || string.equals("false"))
                return getBoolean(DropStringUtils.removeNumSuffix(string), processData);
            return getInteger(DropStringUtils.removeNumSuffix(string), processData);
        } catch (Exception e) {}

        try {
            String[] valuesString = string.split(":");
            if (valuesString.length >= 1) {
                int type = 0;
                if (valuesString[0].endsWith("b") || valuesString[0].endsWith("B")) type = 1;

                int[] valuesInt = new int[valuesString.length];
                byte[] valuesByte = new byte[valuesString.length];
                for (int b = 0; b < valuesString.length; b++) {
                    if (type == 0) valuesInt[b] = getInteger(valuesString[b], processData);
                    else {
                        valuesString[b] = DropStringUtils.removeNumSuffix(valuesString[b]);
                        valuesByte[b] = getByte(valuesString[b], processData);
                    }
                }

                if (type == 0 && valuesInt.length > 0) return valuesInt;
                if (type == 1 && valuesByte.length > 0) return valuesByte;
            }
        } catch (Exception e) {}

        return string;
    }

    public static void setNBTTagValue(INBTBase curTag, String tagName, Object tagValue) {
        if (curTag instanceof NBTTagCompound) {
            NBTTagCompound tagCompound = (NBTTagCompound) curTag;
            if (tagValue instanceof String) tagCompound.setString(tagName, (String) tagValue);
            if (tagValue instanceof Boolean) tagCompound.setBoolean(tagName, (Boolean) tagValue);
            if (tagValue instanceof Integer) tagCompound.setInt(tagName, (Integer) tagValue);
            if (tagValue instanceof Float) tagCompound.setFloat(tagName, (Float) tagValue);
            if (tagValue instanceof Double) tagCompound.setDouble(tagName, (Double) tagValue);
            if (tagValue instanceof Short) tagCompound.setShort(tagName, (Short) tagValue);
            if (tagValue instanceof Byte) tagCompound.setByte(tagName, (Byte) tagValue);
            if (tagValue instanceof int[]) tagCompound.setIntArray(tagName, (int[]) tagValue);
            if (tagValue instanceof byte[]) tagCompound.setByteArray(tagName, (byte[]) tagValue);
            if (tagValue instanceof NBTTagCompound)
                tagCompound.setTag(tagName, (NBTTagCompound) tagValue);
            if (tagValue instanceof NBTTagList) tagCompound.setTag(tagName, (NBTTagList) tagValue);
        }
        if (curTag instanceof NBTTagList) {
            NBTTagList tagList = (NBTTagList) curTag;
            if (tagValue instanceof String) tagList.add(new NBTTagString((String) tagValue));
            if (tagValue instanceof Integer) tagList.add(new NBTTagInt((Integer) tagValue));
            if (tagValue instanceof Float) tagList.add(new NBTTagFloat((Float) tagValue));
            if (tagValue instanceof Double) tagList.add(new NBTTagDouble((Double) tagValue));
            if (tagValue instanceof Short) tagList.add(new NBTTagShort((Short) tagValue));
            if (tagValue instanceof Byte) tagList.add(new NBTTagByte((Byte) tagValue));
            if (tagValue instanceof int[]) tagList.add(new NBTTagIntArray((int[]) tagValue));
            if (tagValue instanceof byte[]) tagList.add(new NBTTagByteArray((byte[]) tagValue));
            if (tagValue instanceof NBTTagCompound) tagList.add((NBTTagCompound) tagValue);
            if (tagValue instanceof NBTTagList) tagList.add((NBTTagList) tagValue);
        }
    }

    public static Item getItem(String name, DropProcessData processData) {
        String fullName = ValueParser.getString(name, processData);
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(fullName));
    }

    public static Block getBlock(String name, DropProcessData processData) {
        String fullName = ValueParser.getString(name, processData);
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(fullName));
    }

    public static ItemStack getItemStack(String name, DropProcessData processData) {
        String[] splitName = name.split("\\*");
        Item item = getItem(splitName[0], null);
        int count = splitName.length > 1
            ? ValueParser.getInteger(splitName[1], processData) : 1;
        return new ItemStack(item, count);
    }
}
