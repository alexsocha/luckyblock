package mod.lucky.drop;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.drop.value.ValueParser;

public class LuckyDropRaw {
    private String rawDrop;
    private int luck = 2;
    private float chance = 1;
    private final Random random;

    public LuckyDropRaw(String rawDrop) {
        if ((rawDrop.startsWith("(") && rawDrop.endsWith(")"))
            || (rawDrop.startsWith("[") && rawDrop.endsWith("]"))
            || (rawDrop.startsWith("{") && rawDrop.endsWith("}")))
            rawDrop = rawDrop.substring(1, rawDrop.length() - 1);

        this.rawDrop = rawDrop;
        this.random = new Random();

        try {
            this.getDropProperties();
        } catch (Exception e) {
            System.out.println(
                "The Lucky Block encountered and error while loading properties for drop: " + rawDrop);
            e.printStackTrace();
        }
    }

    public LuckyDropRaw() {
        this.random = new Random();
    }

    public void getDropProperties() {
        ArrayList<Integer> points = new ArrayList<Integer>();
        int count = 0;
        while (true) {
            int chancePoint = this.rawDrop.indexOf("@chance=");
            int luckPoint = this.rawDrop.indexOf("@luck=");
            int propertyType;
            int propertyStringLength;

            if ((chancePoint < luckPoint && chancePoint != -1) || (luckPoint == -1 && chancePoint > -1)) {
                propertyType = 0;
                propertyStringLength = "@chance=".length();
                points.add(count, chancePoint);
            } else if ((luckPoint < chancePoint && luckPoint != -1)
                || (chancePoint == -1 && luckPoint > -1)) {
                propertyType = 1;
                propertyStringLength = "@luck=".length();
                points.add(count, luckPoint);

            } else {
                break;
            }

            char[] invalidChars = {'+', '*', '(', ')', ',', ';', '/', '@'};
            int currEndPoint =
                getEndPoint(this.rawDrop, points.get(count) + propertyStringLength, invalidChars);
            String currValue =
                this.rawDrop.substring(points.get(count) + propertyStringLength, currEndPoint);
            if (currValue.startsWith("(") && currValue.endsWith(")"))
                currValue = currValue.substring(1, currValue.length() - 1);

            if (propertyType == 0) this.chance = ValueParser.getFloat(currValue);
            if (propertyType == 1) this.luck = ValueParser.getInteger(currValue);

            String linePart1 = this.rawDrop.substring(0, points.get(count));
            String linePart2 = this.rawDrop.substring(currEndPoint, this.rawDrop.length());
            this.rawDrop = linePart1 + linePart2;

            count++;
        }
    }

    public LuckyDropRaw copy() {
        LuckyDropRaw luckyDropBase = new LuckyDropRaw("");

        luckyDropBase.rawDrop = this.rawDrop;
        luckyDropBase.luck = this.luck;
        luckyDropBase.chance = this.chance;

        return luckyDropBase;
    }

    public String getDropValue() {
        return this.rawDrop;
    }

    public void setDropValue(String dropValue) {
        this.rawDrop = dropValue;
    }

    public int getLuck() {
        return this.luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public float getChance() {
        return this.chance;
    }

    public void setChance(float chance) {
        this.chance = chance;
    }

    @Override
    public String toString() {
        return this.rawDrop;
    }

    private static int getEndPoint(String value, int startPoint, char... invalidChars) {
        char[] chars = value.toCharArray();
        int endPoint = chars.length;
        for (int i = startPoint; i < chars.length; i++) {
            boolean shouldBreak = false;
            for (char invalidChar : invalidChars) {
                if (chars[i] == invalidChar) {
                    endPoint = i;
                    shouldBreak = true;
                    break;
                }
            }
            if (shouldBreak == true) break;
        }
        return endPoint;
    }
}
