package mod.lucky.drop.value;

public class DropStringUtils {
    public static String fixBackslash(String string) {
        string = string.replaceAll("\\\\t", "\t");
        string = string.replaceAll("\\\\b", "\b");
        string = string.replaceAll("\\\\n", "\n");
        string = string.replaceAll("\\\\r", "\r");
        string = string.replaceAll("\\\\f", "\f");
        return string;
    }

    public static boolean hasDecimalPoint(String string) {
        if (string == null) return false;
        string = removeNumSuffix(string);
        for (int i = string.length() - 1; i >= 0; i--) {
            if (Character.isDigit(string.charAt(i))) continue;
            if (string.charAt(i) == '.') return true;
            return false;
        }
        return false;
    }

    public static boolean isGenericFloat(String string) {
        if (string == null) return false;
        if (string.endsWith("f")
            || string.endsWith("F")
            || string.endsWith("d")
            || string.endsWith("D")
            || hasDecimalPoint(string)) return true;
        else return false;
    }

    public static String removeNumSuffix(String string) {
        if (string == null) return null;
        if (string.endsWith("f")
            || string.endsWith("F")
            || string.endsWith("d")
            || string.endsWith("D")
            || string.endsWith("s")
            || string.endsWith("S")
            || string.endsWith("b")
            || string.endsWith("B")) return string.substring(0, string.length() - 1);
        else return string;
    }

    public static String[] splitBracketString(String line, char seperator) {
        // find correct separators
        char[] lineChar = line.toCharArray();
        int point[] = new int[1024];
        int pointCount = 1;
        int bracketTier = 0;
        boolean inQuotes = false;

        point[0] = -1;

        for (int a = 0; a < lineChar.length; a++) {
            boolean charCanceled = a > 0 && lineChar[a - 1] == '\\';
            if (!charCanceled) {
                if (lineChar[a] == '"') {
                    inQuotes = inQuotes == false ? true : false;
                }

                if ((lineChar[a] == '(' || lineChar[a] == '[' || lineChar[a] == '{') && !inQuotes) {
                    bracketTier++;
                }
                if ((lineChar[a] == ')' || lineChar[a] == ']' || lineChar[a] == '}') && !inQuotes) {
                    bracketTier--;
                }
                if (bracketTier < 0) {
                    point[pointCount] = a;
                    break;
                }
                if (lineChar[a] == seperator && bracketTier == 0 && !inQuotes) {
                    point[pointCount] = a;
                    pointCount++;
                }
            }
        }
        if (bracketTier >= 0) point[pointCount] = line.length();

        // divide according to separators
        String[] contents = new String[pointCount];
        for (int a = 0; a < contents.length; a++) {
            contents[a] = line.substring(point[a] + 1, point[a + 1]);
        }

        return contents;
    }

    public static int getEndPoint(String value, int startPoint, char... invalidChars) {
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
