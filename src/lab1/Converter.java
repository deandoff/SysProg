package lab1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converter {
    public static int convertHexToDec(String str) {
        int decNumber = 0;
        str = str.toUpperCase();
        List<Character> hexNumbers = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');

        char[] chrs = str.toCharArray();

        double k = 0;
        for (int i = chrs.length - 1; i >= 0; i--) {
            decNumber += hexNumbers.indexOf(chrs[i]) * (int) (Math.pow(16, k));
            k++;
        }

        return decNumber;
    }

    public static String convertDecToHex(int number) {
        return Integer.toHexString(number).toUpperCase();
    }

    public static String convertToTwoChars(String str) {
        final int lengthNumber = 2;
        var chars = str.toCharArray();
        var sum = new char[lengthNumber];
        String convertNumber = "";

        if (str.equals(""))
            return "";

        if (chars.length <= lengthNumber) {
            int needZero = lengthNumber - chars.length;

            for (int i = lengthNumber - 1; i >= needZero; i--)
                sum[i] = chars[i - needZero];

            for (int i = 0; i < needZero; i++)
                sum[i] = '0';

            for (var s : sum) {
                convertNumber += s;
            }
        }

        return convertNumber;
    }

    public static String convertToSixChars(String str) {
        final int lengthNumber = 6;
        var chars = str.toCharArray();
        var sum = new char[lengthNumber];
        String convertNumber = "";

        if (str.equals(""))
            return "";

        if (chars.length <= lengthNumber) {
            int needZero = lengthNumber - chars.length;

            for (int i = lengthNumber - 1; i >= needZero; i--)
                sum[i] = chars[i - needZero];

            for (int i = 0; i < needZero; i++)
                sum[i] = '0';

            for (var s : sum) {
                convertNumber += s;
            }
        }
        return convertNumber;
    }

    public static String convertASCII(String str) {
        String res = "";
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.US_ASCII);

        for (int i = 0; i < bytes.length; i++) {
            int unsignedByte = bytes[i] & 0xFF;
            res += convertDecToHex(unsignedByte);
        }

        return res;
    }

    public static String convertToBinaryCodeSTART(String address, String command, String OP1) {
        return "H" + " " + address + " " + command + " " + convertToSixChars(convertDecToHex(Integer.parseInt(OP1)));
    }

    public static String convertToBinaryCode(String address, String command, String length, String OP1, String OP2) {
        String str = "";
        str += "T" + " ";

        if (!address.isEmpty())
            str += address + " ";

        str += length + " " + command + " ";

        if (!OP1.isEmpty())
            str += OP1 + " ";
        if (!OP2.isEmpty())
            str += OP2;

        return str;
    }

    public static String convertToBinaryCodeEND(String address) {
        return "E" + " " + address;
    }
}