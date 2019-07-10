package mod.lucky.drop.value;

import java.util.ArrayList;
import java.util.Random;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.init.SetupCommon;
import mod.lucky.structure.Structure;
import mod.lucky.structure.StructureUtils;
import mod.lucky.util.LuckyUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HashVariables {
    private static String[] hashVariables = {
        "#randPotion",
        "#randSpawnEgg",
        "#randColor",
        "#time",
        "#bPosX", "#bPosY", "#bPosZ", "#bPos",
        "#bExactPosX", "#bExactPosY", "#bExactPosZ", "#bExactPos",
        "#ePosX", "#ePosY", "#ePosZ", "#ePos",
        "#eExactPosX", "#eExactPosY", "#eExactPosZ", "#eExactPos",
        "#pPosX", "#pPosY", "#pPosZ", "#pPos",
        "#pExactPosX", "#pExactPosY", "#pExactPosZ", "#pExactPos",
        "#pName", "#pUUID", "#pDirect", "#pYaw", "#pPitch",
        "#posX", "#posY", "#posZ", "#pos",
        "#rotation",
        "#bowPosX", "#bowPosY", "#bowPosZ", "#bowPos"
    };
    private static String[] bracketHashVariables = {
        "#rand(", "#randPosNeg(",
        "#randList(",
        "#circleOffset(",
        "#sPosX(", "#sPosY(", "#sPosZ(", "#sPos(",
        "#drop(",
        "#eval(",
        "#json(", "#jsonStr("
    };

    private static Random random = new Random();
    private static ScriptEngine scriptEngine =
        new ScriptEngineManager(null).getEngineByName("JavaScript");

    public static String processString(String string, DropProcessData processData) {
        if (!string.contains("#") && !string.contains("$")) return string;

        string = processBracketHash(string, processData);

        string = string.replace("#randPotion", LuckyUtils.getRandomPotionId());
        string = string.replace("#randSpawnEgg", LuckyUtils.getRandomSpawnEggId());
        string = string.replace("#randColor", LuckyUtils.getRandomColor());

        if (processData != null) {
            Vec3d harvestPos = processData.getHarvestPos();
            string = string.replace("#bPosX", String.valueOf(Math.floor(harvestPos.x)));
            string = string.replace("#bPosY", String.valueOf(Math.floor(harvestPos.y)));
            string = string.replace("#bPosZ", String.valueOf(Math.floor(harvestPos.z)));
            string = string.replace("#bPos", "("
                + Math.floor(harvestPos.x)
                + "," + Math.floor(harvestPos.y)
                + "," + Math.floor(harvestPos.z) + ")");
            string = string.replace("#bExactPosX", String.valueOf(harvestPos.x));
            string = string.replace("#bExactPosY", String.valueOf(harvestPos.y));
            string = string.replace("#bExactPosZ", String.valueOf(harvestPos.z));
            string =
                string.replace(
                    "#bExactPos",
                    String.valueOf("(" + harvestPos.x + "," + harvestPos.y + "," + harvestPos.z + ")"));

            if (processData.getHitEntity() != null) {
                Vec3d entityPos = processData.getHitEntity().getPositionVector();
                string = string.replace("#ePosX", String.valueOf(Math.floor(entityPos.x)));
                string = string.replace("#ePosY", String.valueOf(Math.floor(entityPos.y)));
                string = string.replace("#ePosZ", String.valueOf(Math.floor(entityPos.z)));
                string = string.replace("#ePos", "("
                    + Math.floor(entityPos.x)
                    + "," + Math.floor(entityPos.y)
                    + "," + Math.floor(entityPos.z) + ")");

                string = string.replace("#eExactPosX", String.valueOf(entityPos.x));
                string = string.replace("#eExactPosY", String.valueOf(entityPos.y));
                string = string.replace("#eExactPosZ", String.valueOf(entityPos.z));
                string =
                    string.replace(
                        "#eExactPos",
                        String.valueOf("(" + entityPos.x + "," + entityPos.y + "," + entityPos.z + ")"));
            }

            if (processData.getWorld() != null) {
                string = string.replace("#time", String.valueOf(processData.getWorld().getDayTime()));
            }

            if (processData.getPlayer() != null) {
                string = string.replace("#pPosX", String.valueOf(Math.floor(processData.getPlayer().posX)));
                string = string.replace("#pPosY", String.valueOf(Math.floor(processData.getPlayer().posY)));
                string = string.replace("#pPosZ", String.valueOf(Math.floor(processData.getPlayer().posZ)));
                string =
                    string.replace(
                        "#pPos",
                        String.valueOf(
                            "("
                                + Math.floor(processData.getPlayer().posX)
                                + ","
                                + Math.floor(processData.getPlayer().posY)
                                + ","
                                + Math.floor(processData.getPlayer().posZ)
                                + ")"));

                string = string.replace("#pExactPosX", String.valueOf(processData.getPlayer().posX));
                string = string.replace("#pExactPosY", String.valueOf(processData.getPlayer().posY));
                string = string.replace("#pExactPosZ", String.valueOf(processData.getPlayer().posZ));
                string =
                    string.replace(
                        "#pExactPos",
                        String.valueOf(
                            "("
                                + processData.getPlayer().posX
                                + ","
                                + processData.getPlayer().posY
                                + ","
                                + processData.getPlayer().posZ
                                + ")"));

                string =
                    string.replace("#pName", processData.getPlayer().getName().getUnformattedComponentText());
                string = string.replace("#pUUID", processData.getPlayer().getUniqueID().toString());
                int playerRotation =
                    (int) Math.round((processData.getPlayer().getRotationYawHead() + 180.0D) / 90.0D) % 4;
                if (playerRotation < 0) playerRotation += 4;
                string = string.replace("#pDirect", String.valueOf(playerRotation));
                string =
                    string.replace("#pYaw", String.valueOf(processData.getPlayer().getRotationYawHead()));
                string = string.replace("#pPitch", String.valueOf(processData.getPlayer().rotationPitch));

                ArrowEntity entityArrow;
                if (processData.getPlayer() instanceof LivingEntity)
                    entityArrow =
                        new ArrowEntity(
                            processData.getWorld(), (LivingEntity) processData.getPlayer());
                else
                    entityArrow =
                        new ArrowEntity(
                            processData.getWorld(),
                            processData.getPlayer().posX,
                            processData.getPlayer().posY,
                            processData.getPlayer().posZ);
                string = string.replace("#bowPosX", String.valueOf(entityArrow.getPositionVector().x));
                string = string.replace("#bowPosY", String.valueOf(entityArrow.getPositionVector().y));
                string = string.replace("#bowPosZ", String.valueOf(entityArrow.getPositionVector().z));
                string =
                    string.replace(
                        "#bowPos",
                        String.valueOf(
                            "("
                                + entityArrow.getPositionVector().x
                                + ","
                                + entityArrow.getPositionVector().y
                                + ","
                                + entityArrow.getPositionVector().z
                                + ")"));
            }
        }

        string = string.replace("'#'", "#");
        string = string.replace("'@'", "@");
        string = string.replace("'$'", "'\u00A7'");
        string = string.replace("$", "\u00A7");
        string = string.replace("'\u00A7'", "$");

        return string;
    }

    private static String fixBackslash(String value) {
        value = value.replace("\\\\t", "\t");
        value = value.replace("\\\\b", "\b");
        value = value.replace("\\\\n", "\n");
        value = value.replace("\\\\r", "\r");
        value = value.replace("\\\\f", "\f");
        return value;
    }

    private static String getBracketHashValue(String string, DropProcessData processData) {
        try {
            String propertiesString = string.substring(string.indexOf('(') + 1, string.length() - 1);
            String[] properties = DropStringUtils.splitBracketString(propertiesString, ',');
            String type = string.substring(0, string.indexOf('('));

            if (type.equals("#rand") || type.equals("#randPosNeg")) {
                boolean isFloat =
                    DropStringUtils.isGenericFloat(properties[0])
                        || DropStringUtils.isGenericFloat(properties[1]);
                properties[0] = DropStringUtils.removeNumSuffix(properties[0]);
                properties[1] = DropStringUtils.removeNumSuffix(properties[1]);

                if (isFloat) {
                    float min = ValueParser.getFloat(properties[0], processData);
                    float max = ValueParser.getFloat(properties[1], processData);
                    float num = HashVariables.randomFloatClamp(random, min, max);
                    if (type.equals("#randPosNeg") && random.nextInt(2) == 0) num *= -1;
                    return String.valueOf(num);
                } else {
                    int min = ValueParser.getInteger(properties[0], processData);
                    int max = ValueParser.getInteger(properties[1], processData);
                    int num = random.nextInt((max - min) + 1) + min;
                    if (type.equals("#randPosNeg") && random.nextInt(2) == 0) num *= -1;
                    return String.valueOf(num);
                }

            } else if (type.equals("#randList")) {
                int index = random.nextInt(properties.length);
                return ValueParser.getString(properties[index], processData);

            } else if (type.equals("#circleOffset")) {
                int min = 0;
                int max = 0;
                if (properties.length == 1) {
                    max = ValueParser.getInteger(properties[0], processData);
                    min = max;
                } else if (properties.length == 2) {
                    min = ValueParser.getInteger(properties[0], processData);
                    max = ValueParser.getInteger(properties[1], processData);
                }
                int radius = random.nextInt((max - min) + 1) + min;
                int angle = random.nextInt(360);
                int length = (int) Math.round(radius * Math.sin(Math.toRadians(angle)));
                int width = (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
                return "(" + length + "," + 0 + "," + width + ")";

            } else if (type.equals("#eval")) {
                Object value = scriptEngine.eval(processString(properties[0], processData));
                String result = String.valueOf(value);
                // handle integer-double confusion
                if (value instanceof Double && result.endsWith(".0"))
                    result = result.substring(0, result.length() - 2);
                else result = String.valueOf(value);
                return fixBackslash(result);

            } else if (type.equals("#json") || type.equals("#jsonStr")) {
                String nbtStr = String.join(",", properties);
                if (Character.isLetter(nbtStr.toCharArray()[0]))
                    nbtStr = "(" + nbtStr + ")";

                try {
                    INBT nbt = ValueParser.getNBTBase(nbtStr, processData);
                    JsonElement jsonEl = ValueParser.nbtToJson(nbt);

                    if (type.equals("#json")) return jsonEl.toString();
                    else {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("", jsonEl.toString());
                        String jsonObjStr = jsonObj.toString();

                        return jsonObjStr.substring("{\"\":".length(),
                            jsonObjStr.length() - 1);
                    }
                } catch (Exception e) {
                    Lucky.error(e, "Error evaluating NBT as JSON: " + nbtStr);
                }

            } else if (processData != null
                && processData.getProcessType() == DropProcessData.EnumProcessType.LUCKY_STRUCT
                && processData.getDropSingle() != null
                && (type.equals("#sPosX")
                || type.equals("#sPosY")
                || type.equals("#sPosZ")
                || type.equals("#sPos"))) {
                boolean isFloat =
                    DropStringUtils.isGenericFloat(properties[0])
                        || DropStringUtils.isGenericFloat(properties[1])
                        || DropStringUtils.isGenericFloat(properties[2]);
                properties[0] = DropStringUtils.removeNumSuffix(properties[0]);
                properties[1] = DropStringUtils.removeNumSuffix(properties[1]);
                properties[2] = DropStringUtils.removeNumSuffix(properties[2]);

                DropSingle drop = processData.getDropSingle();
                Structure structure = SetupCommon.getStructure(drop.getPropertyString("ID"));
                if (structure == null) return "";
                Vec3d harvestPos =
                    new Vec3d(
                        drop.getPropertyFloat("posX"),
                        drop.getPropertyFloat("posY"),
                        drop.getPropertyFloat("posZ"));
                int rotation = drop.getPropertyInt("rotation");

                if (isFloat) {
                    Vec3d structCoordPos =
                        new Vec3d(
                            ValueParser.getFloat(properties[0], processData),
                            ValueParser.getFloat(properties[1], processData),
                            ValueParser.getFloat(properties[2], processData));
                    Vec3d worldPos =
                        StructureUtils.getWorldPos(
                            structCoordPos, structure.centerPos, harvestPos, rotation);

                    if (type.equals("#sPosX")) return String.valueOf((float) worldPos.x);
                    if (type.equals("#sPosY")) return String.valueOf((float) worldPos.y);
                    if (type.equals("#sPosZ")) return String.valueOf((float) worldPos.z);
                    if (type.equals("#sPos"))
                        return String.valueOf(
                            String.valueOf(
                                "("
                                    + (float) worldPos.x
                                    + ","
                                    + (float) worldPos.y
                                    + ","
                                    + (float) worldPos.z
                                    + ")"));
                } else {
                    BlockPos structCoordPos =
                        new BlockPos(
                            ValueParser.getInteger(properties[0], processData),
                            ValueParser.getInteger(properties[1], processData),
                            ValueParser.getInteger(properties[2], processData));
                    BlockPos worldPos =
                        StructureUtils.getWorldPos(
                            structCoordPos, structure.centerPos, harvestPos, rotation);
                    if (type.equals("#sPosX")) return String.valueOf(Math.round(worldPos.getX()));
                    if (type.equals("#sPosY")) return String.valueOf(Math.round(worldPos.getY()));
                    if (type.equals("#sPosZ")) return String.valueOf(Math.round(worldPos.getZ()));
                    if (type.equals("#sPos"))
                        return String.valueOf(
                            String.valueOf(
                                "("
                                    + Math.round(worldPos.getX())
                                    + ","
                                    + Math.round(worldPos.getY())
                                    + ","
                                    + Math.round(worldPos.getZ())
                                    + ")"));
                }

            } else if (processData != null
                && processData.getProcessType() == DropProcessData.EnumProcessType.LUCKY_STRUCT
                && processData.getDropSingle() != null
                && (type.equals("#drop"))) {
                String name = properties[0];
                return processData.getDropSingle().getProperty(name).toString();
            }

        } catch (Exception e) {
            Lucky.error(e, "Error processing hash variable: " + string);
        }

        return "";
    }

    private static String processBracketHash(String value, DropProcessData processData) {
        ArrayList<Integer> points = new ArrayList<Integer>();
        int pointCount = 0;
        while (true) {
            int minPoint = -1;
            int minHashIndex = -1;
            String hashType;
            int hashLength;

            for (int i = 0; i < bracketHashVariables.length; i++) {
                int point =
                    value.indexOf(
                        bracketHashVariables[i], pointCount == 0 ? 0 : points.get(pointCount - 1) + 1);
                if (point != -1 && (minPoint == -1 || point < minPoint)) {
                    minPoint = point;
                    minHashIndex = i;
                }
            }
            if (minPoint == -1) break;
            hashType = bracketHashVariables[minHashIndex];
            hashLength = bracketHashVariables[minHashIndex].length() - 1;
            points.add(pointCount, minPoint);

            String curHash = "";
            int curEndPoint = -1;

            char[] lineChar = value.toCharArray();
            int bracketTier = 0;
            boolean inQuotesBefore = false;
            boolean inQuotesNow = false;

            char[] newLine = new char[lineChar.length];
            int writeIndex = 0;

            for (int i = 0; i < lineChar.length; i++) {
                boolean charCanceled = i > 0 && lineChar[i - 1] == '\\';
                newLine[writeIndex] = lineChar[i];

                if (i >= points.get(pointCount) + hashLength) {
                    if (!charCanceled) {
                        if ((lineChar[i] == '"')) inQuotesNow = !inQuotesNow;
                        if ((lineChar[i] == '(' || lineChar[i] == '[' || lineChar[i] == '{') && !inQuotesNow) {
                            bracketTier++;
                        }
                        if ((lineChar[i] == ')' || lineChar[i] == ']' || lineChar[i] == '}') && !inQuotesNow) {
                            bracketTier--;
                        }
                        if (bracketTier == 0) {
                            char[] newLineFinal = new char[writeIndex + 1];
                            for (int j = 0; j < newLineFinal.length; j++) newLineFinal[j] = newLine[j];
                            curHash = new String(newLineFinal);
                            curEndPoint = i;
                            break;
                        }
                    }

                    // undo cancelled quote and set in quotes
                    if ((lineChar[i] == '"') && charCanceled && inQuotesBefore) {
                        writeIndex--;
                        newLine[writeIndex] = '"';
                        boolean charDoubleCanceled = i >= 2 && lineChar[i - 2] == '\\';
                        if (!charDoubleCanceled) inQuotesNow = !inQuotesNow;
                    }
                } else {
                    if ((lineChar[i] == '"') && !charCanceled) inQuotesBefore = !inQuotesBefore;
                }
                writeIndex++;
            }
            curHash = curHash.substring(points.get(pointCount), curHash.length());

            String result = getBracketHashValue(curHash, processData);
            String valuePart1 = value.substring(0, points.get(pointCount));
            String valuePart2 = value.substring(curEndPoint + 1, value.length());
            value = valuePart1 + result + valuePart2;

            pointCount++;
        }
        return value;
    }

    public static boolean containsHashVariables(String string) {
        for (String hashVariable : hashVariables) if (string.contains(hashVariable)) return true;
        for (String hashVariable : bracketHashVariables) if (string.contains(hashVariable)) return true;
        for (String hashVariable : CustomNBTTags.nbtHashVariables)
            if (string.contains(hashVariable)) return true;
        return false;
    }

    public static String autoCancelHash(String s) {
        // Auto cancel hash
        s = s.replace("#", "'#'");
        s = s.replace("''#''", "'#'");
        s = s.replace("['#']", "#");
        return s;
    }

    public static float randomFloatClamp(Random random, float min, float max) {
        return (float) (min + (max - min) * random.nextDouble());
    }
}
