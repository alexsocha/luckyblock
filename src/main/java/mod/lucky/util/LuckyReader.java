package mod.lucky.util;

import mod.lucky.Lucky;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuckyReader {
    private BufferedReader bufferedReader;

    public LuckyReader(InputStreamReader reader) {
        this.bufferedReader = new BufferedReader(reader);
    }

    public String readLine() {
        try {
            String finalString = "";
            String curString = null;
            while ((curString = this.bufferedReader.readLine()) != null) {
                curString = curString.trim();
                if (curString.startsWith("/") || curString.equals("")) continue;
                if (curString.endsWith("\\")) {
                    curString = curString.substring(0, curString.length() - 1).trim();
                    finalString += curString;
                    continue;
                } else finalString += curString;
                break;
            }

            if (curString == null) return null;
            return finalString;
        } catch (IOException e) {
            Lucky.error(e, "I/O read Error");
        }
        return "";
    }

    public void close() {
        try {
            this.bufferedReader.close();
        } catch (IOException e) {
            Lucky.error(e, "I/O close Error");
        }
    }
}
