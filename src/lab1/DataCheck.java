package lab1;

import java.util.Objects;

public class DataCheck {
    public boolean checkLettersAndNumbers(String str) {
        if (str != null) {
            str = str.toUpperCase();
        } else {
            return false;
        }

        char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        return cycleFor(str, chars);
    }

    private boolean cycleFor(String str, char[] chars) {
        for (int i = 0; i < str.length(); i++) {
            if (indexOf(chars, str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    private int indexOf(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private int indexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], target)) {
                return i;
            }
        }
        return -1;
    }

    public boolean checkNumbers(String str) {
        char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

        return cycleFor(str, chars);
    }

    public boolean checkLetters(String str) {
        if (str != null)
            str = str.toUpperCase();
        else
            return false;

        char[] chars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        return cycleFor(str, chars);
    }

    public boolean checkAddress(String str) {
        if (!str.isEmpty()) {
            str = str.toUpperCase();
            char[] chars = { 'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
            return cycleFor(str, chars);
        } else
            return false;
    }

    public boolean checkRegisters(String str) {
        if (str == null)
            return false;

        str = str.toUpperCase();
        String[] registers = { "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9",
                "R10", "R11", "R12", "R13", "R14", "R15" };

        return indexOf(registers, str) != -1;
    }


    public int getRegisters(String str) {
        str = str.toUpperCase();
        String[] registers = { "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15" };
        return indexOf(registers, str);
    }

    public boolean checkDirective(String str) {
        if (str != null) {
            str = str.toUpperCase();
        } else {
            return false;
        }

        String[] directives = { "START", "END", "BYTE", "WORD", "RESB", "RESW" };

        return indexOf(directives, str) != -1;
    }

    public String checkAndGetString(String str) {
        if ((str.length() > 3) && (str.charAt(0) == 'C') && (str.charAt(1) == '"') && (str.charAt(str.length() - 1) == '"')) {
            return str.substring(2, str.length() - 1);
        }
        return "";
    }


    public String checkAndGetByteString(String str) {
        if ((str.length() > 3) && (str.charAt(0) == 'X') && (str.charAt(1) == '"') && (str.charAt(str.length() - 1) == '"')) {
            String text = str.substring(2, str.length() - 1);
            if (!checkAddress(text))
                return "";
            return text;
        }
        return "";
    }

    public boolean checkRow(String[][] sc, int numb, String[] output, String nameProg) {
        String mark = sc[numb][0];
        String OC = sc[numb][1];
        String OP1 = sc[numb][2];
        String OP2 = sc[numb][3];

        output[0] = mark;
        output[1] = OC;
        output[2] = OP1;
        output[3] = OP2;

        if (checkDirective(mark) || checkRegisters(mark))
            return false;

        if (numb > 0 && nameProg != null && nameProg.equals(mark.toUpperCase()))
            return false;

        if((checkLettersAndNumbers(mark) || mark.isEmpty()) &&
                checkLettersAndNumbers(OC) &&
                (checkLettersAndNumbers(OP2) || OP2.isEmpty())) {

            if (!mark.isEmpty()) {
                if (checkLetters(Character.toString(mark.charAt(0))))
                    return true;
                else
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }
}
