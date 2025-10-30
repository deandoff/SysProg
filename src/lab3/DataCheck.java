package lab3;

import java.util.List;
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

        String[] directives = { "START", "END", "BYTE", "WORD", "RESB", "RESW", "CSECT", "EXTREF", "EXTDEF" };

        return indexOf(directives, str) != -1;
    }

    public String checkAndGetString(String OP1) {
        if (OP1 != null && OP1.length() > 3
                && OP1.charAt(0) == 'C'
                && OP1.charAt(1) == '"'
                && OP1.charAt(OP1.length() - 1) == '"') {
            return OP1.substring(2, OP1.length() - 1);
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

        System.out.println("=== DEBUG checkRow ===");
        System.out.println("mark='" + mark + "' OC='" + OC + "' OP1='" + OP1 + "' OP2='" + OP2 + "'");

        if (OC.equalsIgnoreCase("EXTDEF") || OC.equalsIgnoreCase("EXTREF") ||
                OC.equalsIgnoreCase("CSECT") || OC.equalsIgnoreCase("START") ||
                OC.equalsIgnoreCase("END")) {

            System.out.println("  Это директива: " + OC);

            if ((OC.equalsIgnoreCase("EXTDEF") || OC.equalsIgnoreCase("EXTREF") ||
                    OC.equalsIgnoreCase("END")) && !mark.isEmpty()) {
                System.out.println("  ОШИБКА: метка не пустая для " + OC);
                return false;
            }

            if ((OC.equalsIgnoreCase("START") || OC.equalsIgnoreCase("CSECT")) &&
                    mark.isEmpty()) {
                System.out.println("  ОШИБКА: метка пустая для " + OC);
                return false;
            }

            System.out.println("  Проверка директивы пройдена");
        }

        if (checkDirective(mark) || checkRegisters(mark)) {
            System.out.println("  ОШИБКА: метка является директивой или регистром");
            return false;
        }

        if (numb > 0 && nameProg != null && nameProg.equals(mark.toUpperCase())) {
            System.out.println("  ОШИБКА: конфликт с именем программы");
            return false;
        }

        boolean checkMain = (checkLettersAndNumbers(mark) || mark.isEmpty()) &&
                checkLettersAndNumbers(OC) &&
                (checkLettersAndNumbers(OP1) || OP1.isEmpty()) &&
                (checkLettersAndNumbers(OP2) || OP2.isEmpty());

        System.out.println("  Основная проверка: " + checkMain);
        System.out.println("  mark check: " + (checkLettersAndNumbers(mark) || mark.isEmpty()));
        System.out.println("  OC check: " + checkLettersAndNumbers(OC));
        System.out.println("  OP1 check: " + (checkLettersAndNumbers(OP1) || OP1.isEmpty()));
        System.out.println("  OP2 check: " + (checkLettersAndNumbers(OP2) || OP2.isEmpty()));

        if (checkMain) {
            if (!mark.isEmpty()) {
                boolean firstCharCheck = checkLetters(Character.toString(mark.charAt(0)));
                System.out.println("  Первый символ метки: " + firstCharCheck);
                return firstCharCheck;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean checkExternalNames(List<List<String>> symbolTable, String currentSection) {
        for (int i = 0; i < symbolTable.get(0).size(); i++) {
            String section = symbolTable.get(2).get(i);
            String type = symbolTable.get(3).get(i);
            String address = symbolTable.get(1).get(i);

            if (currentSection.equals(section) && "ВНИ".equals(type) && address.isEmpty()) {
                System.out.println("Ошибка: EXTDEF '" + symbolTable.get(0).get(i) + "' в секции " + section + " не имеет адреса");
                return false;
            }
        }
        return true;
    }
}
