package lab3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class Core extends Pass {

    private DataCheck dC = new DataCheck();

    private boolean addCheckError(int i, int numbToAdd, String OC, String OP1, String OP2) {
        if (countAddress + numbToAdd > memoryMax) {
            errorText = "В строке " + (i + 1) + " ошибка. Произошло переполнение";
            return false;
        }

        addToSupportTable(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), OC, OP1, OP2);
        countAddress += numbToAdd;

        if (!checkMemory())
            return false;

        return true;
    }

    public boolean doFirstPass(String[][] sourceCode, String[][] operationCode, JTable supportTableJT, JTable symbolTableJT, int typeAdr) {

        startAddress = 0;
        endAddress = 0;
        countAddress = 0;

        String prevOC = "";
        String OC = "";

        List<String> sectionNames = new ArrayList<>();
        List<String> externalDefNames = new ArrayList<>();
        List<String> externalRefNames = new ArrayList<>();

        symbolTable.clear();
        supportTable.clear();
        endSection.clear();
        sectionNames.clear();
        externalDefNames.clear();
        externalRefNames.clear();

        symbolTable.add(new ArrayList<String>());
        symbolTable.add(new ArrayList<String>());
        symbolTable.add(new ArrayList<String>());
        symbolTable.add(new ArrayList<String>());

        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());

        int numberRows = sourceCode.length;

        boolean flagStart = false;
        boolean flagEnd = false;

        int CSECTCount = 0;
        int countStart = 0;

        String currentCsectName = "";

        for (int i = 0; i < numberRows; i++) {
            if (flagStart) {
                if (countAddress > memoryMax) {
                    errorText = "В строке"+ (i + 1) +" ошибка. Произошло переполнение";
                    return false;
                }
            }

            if (flagEnd) {
                break;
            }

            prevOC = OC;

            String[] rowData = new String[4];
            if (!dC.checkRow(sourceCode, i, rowData, nameProg) && i == 0) {
                errorText = "В строке"+ (i + 1) +" синтаксическая ошибка.";
                return false;
            }



            System.out.println(Arrays.toString(rowData));

            String mark = rowData[0];
            OC = rowData[1];
            String OP1 = rowData[2];
            String OP2 = rowData[3];

            if (!OC.equalsIgnoreCase("EXTDEF") && !OC.equalsIgnoreCase("EXTREF")) {
                String[] strchHolder = new String[]{""};
                int markRow = -1;
                if (!mark.isEmpty()) {
                    markRow = findMarkInMarkTable(mark, strchHolder, currentCsectName);
                }
                String strch = strchHolder[0];

                if ("Er".equals(strch)) {
                    errorText = "В строке " + (i + 1) + " ошибка. Метка уже существует: " + mark;
                    return false;
                }

                if ("mk".equals(strch)) {
                    if (flagStart && !OC.equalsIgnoreCase("CSECT")) {
                        if (!symbolTable.get(2).get(markRow).equalsIgnoreCase(currentCsectName)) {
                            errorText = "В строке " + (i + 1) + " ошибка. Внешнее имя описано не в своей управляющей секции";
                            return false;
                        }
                        symbolTable.get(1).set(markRow,
                                Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                    }
                }

                if ("".equals(strch) && !mark.equalsIgnoreCase(nameProg) && flagStart && !OC.equalsIgnoreCase("CSECT")) {
                    if (!mark.isEmpty() && flagStart && !OC.equalsIgnoreCase("CSECT")) {
                        addSymbolIfNotExists(mark, countAddress, currentCsectName, "");
                    }
                }
            } else {
                if (!mark.isEmpty()) {
                    errorText = "В строке " + (i + 1) + " ошибка. Поле метки не используется для EXTDEF/EXTREF";
                    return false;
                }
            }

            if (!mark.isEmpty()) {
                if (!mark.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
                    errorText = "В строке " + (i + 1) + " ошибка: недопустимые символы в имени метки '" + mark + "'";
                    return false;
                }
            }

            if (!mark.isEmpty()) {
                for (String[] op : operationCode) {
                    if (mark.equalsIgnoreCase(op[0])) {
                        errorText = "В строке " + (i + 1) + " ошибка: метка '" + mark + "' совпадает с названием команды";
                        return false;
                    }
                }
            }

            if (isDirective(mark)) {
                errorText = "В строке " + (i + 1) + " ошибка: метка '" + mark + "' совпадает с названием директивы";
                return false;
            }

            // Проверка на совпадение с регистрами
            if (isRegister(mark)) {
                errorText = "В строке " + (i + 1) + " ошибка: метка '" + mark + "' совпадает с названием регистра";
                return false;
            }

// --- Проверка пустых операндов ---
            if (OP1.contains("[]") || OP2.contains("[]")) {
                errorText = "В строке " + (i + 1) + " ошибка: пустой операнд '[]'";
                return false;
            }

// --- Проверка операнда на соответствие типу адресации ---
            if (typeAdr == 0 && OP1.contains("[") && OP1.contains("]")) {
                errorText = "В строке " + (i + 1) + " ошибка: относительная адресация при выборе прямой";
                return false;
            }
            if (typeAdr == 1 && (!OP1.contains("[") || !OP1.contains("]")) && !dC.checkRegisters(OP1) && !isNumeric(OP1)) {
                errorText = "В строке " + (i + 1) + " ошибка: прямая адресация при выборе относительной";
                return false;
            }
            else {
                if (dC.checkDirective(OC)) {
                    switch (OC) {
                        case "START": {
                            // Если START ещё не встречался
                            if (!flagStart) {
                                flagStart = true;

                                // Проверяем, что указан адрес
                                if (OP1.isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Не указан адрес начала программы.";
                                    return false;
                                }

                                // Проверяем корректность адреса
                                if (!dC.checkAddress(OP1)) {
                                    try {
                                        int op = Integer.parseInt(OP1);
                                        if (op < 0) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Адрес начала не может быть отрицательным.";
                                            return false;
                                        }
                                    } catch (NumberFormatException e) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Невозможно преобразовать адрес начала.";
                                        return false;
                                    }
                                }

                                countAddress = Converter.convertHexToDec(OP1);
                                startAddress = countAddress;

                                if (countAddress != 0) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Адрес начала программы должен быть равен нулю";
                                    return false;
                                }
                                if (mark.isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Не задано имя программы";
                                    return false;
                                }
                                if (mark.length() > 10) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Превышена длина имени программы (не более 10 символов)";
                                    return false;
                                }

                                addToSupportTable(mark, OC, Converter.convertToSixChars(OP1), "");
                                nameProg = mark;

                                if (!OP2.isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы START не рассматривается.";
                                    return false;
                                }

                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Повторное использование директивы START.";
                                return false;
                            }
                            nameProg = mark;
                            currentCsectName = nameProg;
                            break;
                        }
                        case "CSECT": {
                            CSECTCount++;

                            if (!flagStart) {
                                errorText = "В строке " + (i + 1) + " ошибка. Программа не может начинаться с управляющей секции";
                                return false;
                            }

                            endSection.add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                            int oldAddressCount = countAddress;
                            countAddress = 0;
                            startAddress = countAddress;

                            if (dC.checkLettersAndNumbers(OP1) || OP1.isEmpty()) {

                                if (OP1.isEmpty()) {
                                    OP1 = "0";
                                }

                                if (countAddress > 0) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Адрес начала программы должен быть равен 0";
                                    return false;
                                }

                                if (mark.isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Не задано имя программы";
                                    return false;
                                }

                                if (mark.length() > 10) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Превышена длина имени программы";
                                    return false;
                                }

                                for (String extName : externalDefNames) {
                                    if (mark.equalsIgnoreCase(extName)) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Имя секции не может совпадать с внешним именем";
                                        return false;
                                    }
                                }

                                for (String[] strings : operationCode) {
                                    if (mark.equalsIgnoreCase(strings[0])) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Имя секции не может совпадать с названием команды";
                                        return false;
                                    }
                                }

                                for (String str : sectionNames) {
                                    if (str.equals(mark)) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Имя секции совпадает с системным именем";
                                        return false;
                                    }
                                }

                                if (dC.checkDirective(OP1) || dC.checkRegisters(OP1)) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Имя секции совпадает с системным именем";
                                    return false;
                                }

                                addToSupportTable(
                                        Converter.convertToSixChars(Converter.convertDecToHex(oldAddressCount)),
                                        OC,
                                        mark,
                                        ""
                                );

                                nameProg = mark;
                                sectionNames.add(mark);

                                if (!OP2.isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы CSECT не рассматривается. Устраните и повторите заново.";
                                    return false;
                                }

                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес начала программы";
                                return false;
                            }

                            nameProg = mark;
                            currentCsectName = nameProg;
                            break;
                        }

                        case "EXTREF": {
                            if (prevOC.equalsIgnoreCase("EXTDEF") || prevOC.equalsIgnoreCase("EXTREF")
                                    || prevOC.equalsIgnoreCase("START") || prevOC.equalsIgnoreCase("CSECT")) {

                                if (!OP1.isEmpty()) {

                                    if (!dC.checkLettersAndNumbers(OP1)) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Недопустимые символы в операнде";
                                        return false;
                                    }

                                    if (!dC.checkLetters(String.valueOf(OP1.charAt(0)))) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Внешняя ссылка не должна начинаться с цифры";
                                        return false;
                                    }

                                    if (OP1.length() > 10) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Превышена длина имени внешней ссылки";
                                        return false;
                                    }

                                    // Проверка совпадений с командами
                                    for (int j = 0; j < operationCode.length; j++) {
                                        if (OP1.equalsIgnoreCase(operationCode[j][0])) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки не может совпадать с названием команды";
                                            return false;
                                        }
                                    }

                                    // Проверка совпадений с секциями
                                    for (String section : sectionNames) {
                                        if (section.equals(OP1)) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки совпадает с именем секции";
                                            return false;
                                        }
                                    }

                                    // Проверка совпадений с системными именами
                                    if (dC.checkDirective(OP1) || dC.checkRegisters(OP1)) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки совпадает с системным именем";
                                        return false;
                                    }

                                    // Проверка совпадений в таблице символов
                                    for (int j = 0; j < symbolTable.get(0).size(); j++) {
                                        String symCsect = symbolTable.get(2).size() > j ? symbolTable.get(2).get(j) : "";
                                        String symName  = symbolTable.get(0).size() > j ? symbolTable.get(0).get(j) : "";

                                        if (nameProg.equalsIgnoreCase(symCsect) && OP1.equalsIgnoreCase(symName)) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки не может совпадать с внешним именем в одной секции";
                                            return false;
                                        }
                                    }


                                    addToSymbolTable(OP1, "", nameProg.toUpperCase(), "ВНС");
                                    addToSupportTable(mark, OC, OP1, "");
                                    externalRefNames.add(OP1);

                                    if (!OP2.isEmpty()) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Второй операнд директивы EXTREF не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }

                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Не задана внешняя ссылка.";
                                    return false;
                                }

                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Неверная позиция EXTREF";
                                return false;
                            }

                            break;
                        }

                        case "EXTDEF": {
                            if (prevOC.equalsIgnoreCase("EXTDEF") || prevOC.equalsIgnoreCase("START")
                                    || prevOC.equalsIgnoreCase("CSECT")) {

                                if (!OP1.isEmpty()) {

                                    if (!dC.checkLettersAndNumbers(OP1)) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Недопустимые символы в операнде";
                                        return false;
                                    }

                                    if (!dC.checkLetters(String.valueOf(OP1.charAt(0)))) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Внешняя ссылка не должна начинаться с цифры";
                                        return false;
                                    }

                                    if (OP1.length() > 10) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Превышена длина имени внешней ссылки";
                                        return false;
                                    }

                                    // Проверка совпадений с командами
                                    for (int j = 0; j < operationCode.length; j++) {
                                        if (OP1.equalsIgnoreCase(operationCode[j][0])) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки не может совпадать с названием команды";
                                            return false;
                                        }
                                    }

                                    // Проверка совпадений с секциями
                                    for (String section : sectionNames) {
                                        if (section.equals(OP1)) {
                                            errorText = "В строке " + (i + 1) + " ошибка. Имя внешней ссылки совпадает с именем секции";
                                            return false;
                                        }
                                    }

                                    addToSymbolTable(OP1, "", nameProg.toUpperCase(), "ВНИ");
                                    addToSupportTable(mark, OC, OP1, "");
                                    externalDefNames.add(OP1);

                                    if (!OP2.isEmpty()) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Второй операнд директивы EXTDEF не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }

                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Не задана внешняя ссылка.";
                                    return false;
                                }

                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Неверная позиция EXTDEF";
                                return false;
                            }

                            break;
                        }

                        case "WORD": {
                            try {
                                int numb = Integer.parseInt(OP1);
                                if (numb >= 0 && numb <= memoryMax) {
                                    if (!addCheckError(i, 3, OC, Integer.toString(numb), "")) {
                                        return false;
                                    }

                                    System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);


                                    if (OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы WORD не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Отрицательное число либо превышено максимальное значение числа";
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                errorText = "В строке " + (i + 1) + " ошибка. Некорректное число в операнде";
                                return false;
                            }
                            break;
                        }
                        case "BYTE": {
                            try {
                                int numb = Integer.parseInt(OP1);
                                if (numb < 0 || numb > 255) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Число должно быть в диапазоне 0-255";
                                    return false;
                                }

                                if (!addCheckError(i, 1, OC, String.valueOf(numb), "")) {
                                    return false;
                                }

                                System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);


                                if (OP2 != null && !OP2.trim().isEmpty()) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                    return false;
                                }

                            } catch (NumberFormatException e) {
                                String symb = dC.checkAndGetString(OP1);
                                System.out.println(symb);
                                if (symb != null && !symb.isEmpty()) {
                                    if (!addCheckError(i, symb.length(), OC, OP1, "")) {
                                        return false;
                                    }
                                    System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                    if (OP2 != null && !OP2.trim().isEmpty()) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    symb = dC.checkAndGetByteString(OP1);
                                    if (symb != null && !symb.isEmpty()) {
                                        if (symb.length() % 2 == 0) {
                                            if (!addCheckError(i, symb.length() / 2, OC, OP1, "")) {
                                                return false;
                                            }
                                            System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                            if (OP2 != null && !OP2.trim().isEmpty()) {
                                                errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                                return false;
                                            }
                                        } else {
                                            errorText = "В строке " + (i + 1) + " ошибка. Невозможно преобразовать BYTE нечетное количество символов: " + symb.length();
                                            return false;
                                        }
                                    } else {
                                        errorText = "В строке " + (i + 1) + " ошибка. Неверный формат строки: " + OP1;
                                        return false;
                                    }
                                }
                            }
                            break;
                        }
                        case "RESB": {
                            try {
                                int numb = Integer.parseInt(OP1);
                                if (numb > 0) {
                                    if (!addCheckError(i, numb, OC, Integer.toString(numb), "")) {
                                        return false;
                                    }
                                    System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                    if (OP2 != null && OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы RESB не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Количество байт равно нулю или меньше нуля";
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                errorText = "В строке " + (i + 1) + " ошибка. Невозможно выполнить преобразование " + OP1;
                                return false;
                            }
                            break;
                        }
                        case "RESW" : {
                            try {
                                int numb = Integer.parseInt(OP1);
                                if (numb > 0) {
                                    if (!addCheckError(i, numb * 3, OC, Integer.toString(numb), "")) {
                                        return false;
                                    }
                                    System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                    if (OP2 != null && OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы RESW не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Количество байт равно нулю или меньше нуля";
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                errorText = "В строке " + (i + 1) + " ошибка. Невозможно выполнить преобразование " + OP1;
                                return false;
                            }
                            break;
                        }
                        case "END": {
                            if (mark.length() > 0) {
                                errorText = "В строке " + (i + 1) + " метка. Устраните и повторите заново.";
                                return false;
                            }

                            if (flagStart && !flagEnd) {
                                flagEnd = true;
                                endSection.add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                if (OP1.length() == 0) {
                                    endAddress = startAddress;
                                    addToSupportTable(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), OC, "0", "");
                                } else {
                                    if (dC.checkAddress(OP1)) {
                                        endAddress = Converter.convertHexToDec(OP1);
                                        if (endAddress >= startAddress && endAddress <= countAddress) {
                                            addToSupportTable(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), OC, OP1, "");
                                            break;
                                        } else {
                                            errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес входа в программу.";
                                            return false;
                                        }
                                    } else {
                                        errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес входа в программу.";
                                        return false;
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    if (OC.length() > 0) {
                        int numb = findCode(OC, operationCode);
                        if (numb > -1) {
                            if ("1".equals(operationCode[numb][2])) {
                                if (!addCheckError(i, 1, Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4)), "", "")) {
                                    return false;
                                }
                                System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                if (OP1.length() > 0 || OP2.length() > 0) {
                                    errorText = "В строке " + (i + 1) + " операнды не рассматривается в команде " + (operationCode[numb][0]) +". Устраните и повторите заново.";
                                    return false;
                                }
                            } else if (operationCode[numb][2].equals("2")) {
                                try {
                                    int number = Integer.parseInt(OP1);

                                    if (number >= 0 && number <= 255)
                                    {
                                        if (!addCheckError(i, 2, Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4)), OP1, ""))
                                            return false;
                                        System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                        if (OP2.length() > 0) {
                                            errorText = "В строке " + (i + 1) + " второй операнд не рассматривается в команде " + operationCode[numb][0] + ".  Устраните иповторите заново.";
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        errorText = "В строке " + (i + 1) + " ошибка. Отрицательное число либо превышено максимальное значение числа";
                                        return false;
                                    }
                                }
                                catch (NumberFormatException e) {
                                    if (dC.checkRegisters(OP1) && dC.checkRegisters(OP2))
                                    {
                                        if (!addCheckError(i, 2, Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4)), OP1, OP2))
                                            return false;
                                        System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X\n", i+1, mark, OC, OP1, OP2, countAddress);

                                    }
                                    else
                                    {
                                        errorText = "В строке " + (i + 1) + " ошибка. Ошибка в команде " + operationCode[numb][0];
                                        return false;
                                    }
                                }
                            } else if (operationCode[numb][2].equals("4")) {
                                if (!addCheckError(i,4, Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4 + 1)), OP1, OP2))
                                    return false;
                                System.out.printf("Line %d: mark=%s OC=%s OP1=%s OP2=%s countAddress=%04X countAddressSixChar=%s\n", i+1, mark, OC, OP1, OP2, countAddress, Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                System.out.println("Hex: " + Converter.convertDecToHex(countAddress));

                                if (OP2.length() > 0) {
                                    errorText = "В строке " + (i + 1) + " второй операнд не рассматривается в команде "+ (operationCode[numb][0]) + ".  Устраните и повторите заново.";
                                    return false;
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " размер команды больше установленного";
                                return false;
                            }
                        } else {
                            errorText = "В строке " + (i + 1) + " синтаксическая ошибка";
                            return false;
                        }
                    }
                }

            }
        }
        if (!dC.checkExternalNames(symbolTable)) {
            errorText = "Найдено не определенное внешнее имя";
            return false;
        }

        if (!flagEnd) {
            errorText = "Не найдена точка входа в программу";
            return false;
        }
        for (int i = 0; i < supportTable.get(0).size(); i++)
        {
            DefaultTableModel supportModel = (DefaultTableModel) supportTableJT.getModel();
            supportModel.addRow(new Object[]{supportTable.get(0).get(i), supportTable.get(1).get(i), supportTable.get(2).get(i), supportTable.get(3).get(i)});
        }

        for (int i = 0; i < symbolTable.get(0).size(); i++) {
            DefaultTableModel symbolModel = (DefaultTableModel) symbolTableJT.getModel();
            symbolModel.addRow(new Object[]{
                    symbolTable.get(0).get(i),  // имя
                    symbolTable.get(1).get(i),  // адрес
                    symbolTable.get(2).get(i),  // секция
                    symbolTable.get(3).get(i)   // тип (ВНИ/ВНС/пусто)
            });
        }

        return true;
    }

    public boolean doSecondPass(JTextArea BC, JTable settingTableJT, int typeAdr) {
        errorText = "";
        BC.setText("");

        DefaultTableModel settingModel = (DefaultTableModel) settingTableJT.getModel();
        settingModel.setRowCount(0);

        List<String> segmentEnd = new ArrayList<>();
        List<String> extref = new ArrayList<>();

        segmentEnd.clear();
        for (var item : endSection) {
            segmentEnd.add(item);
        }

        List<List<String>> settingTable = new ArrayList<>();
        String currentCSECTName = "";

        for (int i = 0; i < supportTable.get(0).size(); i++) {
            String address = supportTable.get(0).get(i);
            String OC = supportTable.get(1).get(i);
            String OP1 = supportTable.get(2).get(i);
            String OP2 = supportTable.get(3).get(i);

            System.out.printf("DEBUG second pass: line=%d address=%s OC=%s OP1=%s OP2=%s%n", i+1, address, OC, OP1, OP2);

            // --- START или CSECT ---
            if (OC.equals("START") || OC.equals("CSECT")) {
                if (OC.equals("START")) {
                    if (!endSection.isEmpty()) {
                        extref.clear();
                        for (int j = 0; j < settingTable.size(); j++) {
                            if (Objects.equals(settingTable.get(j).get(1), currentCSECTName.toUpperCase())) {
                                BC.append(Converter.convertToBinaryCodeSetting(settingTable.get(j).get(0)) + "\r\n");
                            }
                        }

                        currentCSECTName = supportTable.get(0).get(i);

                        BC.append(Converter.convertToBinaryCodeSTART(
                                supportTable.get(0).get(i),
                                supportTable.get(2).get(0),
                                endSection.get(0)) + "\r\n");
                        endSection.remove(0);
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка в START";
                        BC.setText("");
                        return false;
                    }
                }

                if (OC.equals("CSECT")) {
                    System.out.println("DEBUG CSECT at line " + (i + 1) + ":");
                    System.out.println("  endSection = " + endSection);
                    System.out.println("  endSection.size() = " + endSection.size());
                    if (endSection.isEmpty()) {
                        System.out.println("  ERROR: endSection is empty!");
                        // Можно попробовать продолжить с значением по умолчанию или найти корень проблемы
                        errorText = "В строке " + (i + 1) + " ошибка в CSECT: отсутствует END секция";
                        BC.setText("");
                        return false;
                    }
                    if (!endSection.isEmpty()) {
                        extref.clear();
                        for (int j = 0; j < settingTable.size(); j++) {
                            if (settingTable.get(j).get(1).equalsIgnoreCase(currentCSECTName)) {
                                BC.append(Converter.convertToBinaryCodeSetting(settingTable.get(j).get(0)) + "\r\n");
                            }
                        }

                        BC.append(Converter.convertToBinaryCodeEND("000000") + "\r\n");
                        currentCSECTName = supportTable.get(2).get(i);

                        BC.append(Converter.convertToBinaryCodeSTART(
                                supportTable.get(2).get(i), "000000", endSection.get(0)) + "\r\n");
                        endSection.remove(0);
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка в CSECT";
                        BC.setText("");
                        return false;
                    }
                }
            }
            else {
                if (OC.equals("EXTDEF")) {
                    int find = -1;
                    for (int j = 0; j < symbolTable.get(0).size(); j++) {
                        if (OP1.equals(symbolTable.get(0).get(j)) && currentCSECTName.equals(symbolTable.get(2).get(j).toUpperCase()) && "ВНИ".equals(symbolTable.get(3).get(j))) {
                            find = j;
                            break;
                        }
                    }

                    if (find > -1) {
                        BC.append(Converter.convertToBinaryCodeD(
                                Converter.convertToSixChars(symbolTable.get(1).get(find)), OP1) + "\r\n");
                        continue;
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка в EXTDEF";
                        BC.setText("");
                        return false;
                    }
                }

                // --- EXTREF ---
                if (OC.equals("EXTREF")) {
                    extref.add(OP1);
                    BC.append(Converter.convertToBinaryCodeR(OP1) + "\r\n");
                    continue;
                }

                // --- обработка операндов ---
                boolean[] error1 = new boolean[1];       // [0] = error OP1
                boolean[] flagMark1 = new boolean[1]; // [0] = operandLabel OP1
                String[] tuneAddress1 = new String[1];

                boolean[] error2 = new boolean[1];       // [0] = error OP2
                boolean[] flagMark2 = new boolean[1]; // [0] = operandLabel OP2
                String[] tuneAddress2 = new String[1];

                String res = checkOP(OP1, error1, flagMark1, tuneAddress1, i, currentCSECTName);

                if (error1[0]) {
                    errorText = "В строке " + (i + 1) + " ошибка. Ошибка1 при вычислении операндной части.";
                    BC.setText("");
                    return false;
                }

                settingTable(tuneAddress1[0], currentCSECTName, settingTable);

                String ress = checkOP(OP2, error2, flagMark2, tuneAddress2, i, currentCSECTName);

                if (error2[0]) {
                    errorText = "В строке " + (i + 1) + " ошибка. Ошибка2 при вычислении операндной части.";
                    BC.setText("");
                    return false;
                }
                settingTable(tuneAddress2[0], currentCSECTName, settingTable);

                if (res != null) res = padHexEven(res);
                if (ress != null) ress = padHexEven(ress);

                if (dC.checkDirective(OC)) {
                    switch (OC) {
                        case "RESB":
                            BC.append(Converter.convertToBinaryCode(address, "", res, "", "") + "\r\n");
                            continue;
                        case "RESW":
                            BC.append(Converter.convertToBinaryCode(
                                    address, "", Converter.convertToTwoChars(
                                            Converter.convertDecToHex(Integer.parseInt(OP1) * 3)), "", "") + "\r\n");
                            continue;
                        case "BYTE":
                            BC.append(Converter.convertToBinaryCode(
                                    address, "", Converter.convertToTwoChars(
                                            Converter.convertDecToHex(res.length() + ress.length())), res, ress) + "\r\n");
                            continue;
                        case "WORD":
                            BC.append(Converter.convertToBinaryCode(
                                    address, "", Converter.convertToTwoChars(
                                            Converter.convertDecToHex(Converter.convertToSixChars(res).length() + ress.length())),
                                    Converter.convertToSixChars(res), ress) + "\r\n");
                            continue;
                    }
                } else {
                    System.out.println("OC=" + OC + " type=" + (Converter.convertHexToDec(OC) & 0x03));
                    System.out.println("flagMark=" + flagMark1[0] + " ress='" + ress + "'");
                    int type = Converter.convertHexToDec(OC) & 0x03;
                    if (type == 1) {
                        if (!flagMark1[0]) {
                            errorText = "В строке " + (i + 1) + " ошибка. Для данного типа адресации операнд должен быть меткой";
                            BC.setText("");
                            return false;
                        }
                        if (!Objects.equals(ress, "")) {
                            errorText = "В строке " + (i + 1) + " ошибка. Данный тип адрессации поддерживает один операнд";
                            BC.setText("");
                            return false;
                        }
                    }
                    BC.append(Converter.convertToBinaryCode(address, OC,
                            Converter.convertToTwoChars(Converter.convertDecToHex(OC.length() + res.length() + ress.length())),
                            res, ress) + "\r\n");
                }
            }

            // --- обновление таблицы настройки ---
            if (!settingTable.isEmpty()) {
                DefaultTableModel model = (DefaultTableModel) settingTableJT.getModel();
                model.setRowCount(0);
                for (int j = 0; j < settingTable.size(); j++) {
                    model.addRow(new Object[]{settingTable.get(j).get(0), settingTable.get(j).get(1)});
                }
            }
        }

        for (int j = 0; j < settingTable.size(); j++) {
            if (settingTable.get(j).get(1).toUpperCase().equals(currentCSECTName.toUpperCase())) {
                BC.append(Converter.convertToBinaryCodeSetting(settingTable.get(j).get(0) + "\r\n"));
            }
        }

        BC.append(Converter.convertToBinaryCodeEND(
                Converter.convertToSixChars(Converter.convertDecToHex(endAddress))) + "\r\n");



        if (!errorText.isEmpty()) BC.setText("");
        return true;
    }


    public String checkOP(String OP, boolean[] outFlags, boolean[] outOperandLabel, String[] outAddress, int ind, String csectName) {
        boolean er = false;
        boolean operandLabel = false;
        String address = "";
        String res = "";
        int find = 0;

        if (OP != null && !OP.isEmpty()) {

            if (OP.startsWith("[") && OP.endsWith("]")) {
                String temp = OP.substring(1, OP.length() - 1);

                for (int i = 0; i < symbolTable.get(0).size(); i++) {
                    if (temp.equals(symbolTable.get(0).get(i)) &&
                            csectName.equalsIgnoreCase(symbolTable.get(2).get(i)) &&
                            !"ВНИ".equals(symbolTable.get(3).get(i))) {

                        find++;

                        if ("ВНС".equals(symbolTable.get(3).get(i))) {
                            er = true;
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return "000000";
                        }

                        if (symbolTable.get(3).get(i).isEmpty()) {
                            operandLabel = true;
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return Converter.convertSubHex(symbolTable.get(1).get(i), supportTable.get(0).get(ind + 1));
                        }
                    }
                }

                if (find == 0) {
                    for (int i = 0; i < symbolTable.get(0).size(); i++) {
                        if (temp.equals(symbolTable.get(0).get(i)) &&
                                csectName.equalsIgnoreCase(symbolTable.get(2).get(i)) &&
                                "ВНИ".equals(symbolTable.get(3).get(i))) {

                            operandLabel = true;
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return Converter.convertSubHex(symbolTable.get(1).get(i), supportTable.get(0).get(ind + 1));
                        }
                    }
                }
            }
            // Обычный операнд (не [ ])
            else {
                for (int i = 0; i < symbolTable.get(0).size(); i++) {
                    if (OP.equals(symbolTable.get(0).get(i)) &&
                            csectName.equalsIgnoreCase(symbolTable.get(2).get(i)) &&
                            !"ВНИ".equals(symbolTable.get(3).get(i))) {

                        find++;

                        if ("ВНС".equals(symbolTable.get(3).get(i))) {
                            operandLabel = true;
                            address = supportTable.get(0).get(ind) + " " + symbolTable.get(0).get(i);
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return "000000";
                        }

                        if (symbolTable.get(3).get(i).isEmpty()) {
                            operandLabel = true;
                            address = supportTable.get(0).get(ind);
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return symbolTable.get(1).get(i);
                        }
                    }
                }

                if (find == 0) {
                    for (int i = 0; i < symbolTable.get(0).size(); i++) {
                        if (OP.equals(symbolTable.get(0).get(i)) &&
                                csectName.equalsIgnoreCase(symbolTable.get(2).get(i)) &&
                                "ВНИ".equals(symbolTable.get(3).get(i))) {

                            operandLabel = true;
                            address = supportTable.get(0).get(ind);
                            outFlags[0] = er;
                            outOperandLabel[0] = operandLabel;
                            outAddress[0] = address;
                            return symbolTable.get(1).get(i);
                        }
                    }
                }
            }

            int reg = dC.getRegisters(OP);
            if (reg > -1) {
                outFlags[0] = er;
                outOperandLabel[0] = operandLabel;
                outAddress[0] = address;
                return Converter.convertDecToHex(reg);
            }
            else if (dC.checkNumbers(OP)) {
                outFlags[0] = er;
                outOperandLabel[0] = operandLabel;
                outAddress[0] = address;
                return Converter.convertDecToHex(Integer.parseInt(OP));
            }
            else {
                String str = dC.checkAndGetString(OP);
                if (!str.isEmpty()) {
                    outFlags[0] = er;
                    outOperandLabel[0] = operandLabel;
                    outAddress[0] = address;
                    return Converter.convertASCII(str);
                } else {
                    str = dC.checkAndGetByteString(OP);
                    if (!str.isEmpty()) {
                        outFlags[0] = er;
                        outOperandLabel[0] = operandLabel;
                        outAddress[0] = address;
                        return str;
                    }
                    er = true;
                }
            }
        }

        outFlags[0] = er;
        outOperandLabel[0] = operandLabel;
        outAddress[0] = address;
        return "";
    }

    public void clearTables(JTable supportTableJT, JTable symbolTableJT) {
        for (var list : supportTable) list.clear();
        for (var list : symbolTable) list.clear();

        DefaultTableModel supportModel = (DefaultTableModel) supportTableJT.getModel();
        supportModel.setRowCount(0);

        DefaultTableModel symbolModel = (DefaultTableModel) symbolTableJT.getModel();
        symbolModel.setRowCount(0);

        startAddress = 0;
        endAddress = 0;
        countAddress = 0;
    }

    private String padHexEven(String hex) {
        if (hex == null || hex.isEmpty()) return hex;
        hex = hex.toUpperCase().trim();
        // если это одиночная цифра (например R1→1, R2→2) — добавляем ведущий 0
        if (hex.length() == 1) return "0" + hex;
        // если длина нечётная (например "B") — добавляем ведущий 0
        if (hex.length() % 2 == 1) return "0" + hex;
        return hex;
    }

    public boolean settingTable(String adr, String currentName, List<List<String>> settingTable) {
        if (adr != null && !adr.isEmpty()) {
            int i;
            for (i = 0; i < settingTable.size(); i++) {
                if (adr.equals(settingTable.get(i).get(0))) {
                    return true; // запись с таким адресом уже существует
                }
            }
            // если записи нет, добавляем новую строку
            List<String> newRow = new ArrayList<>();
            newRow.add(adr);
            newRow.add(currentName);
            settingTable.add(newRow);
        }
        return false;
    }


    public boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isDirective(String str) {
        if (str == null) return false;
        String upper = str.toUpperCase();
        return upper.equals("START") || upper.equals("END") || upper.equals("WORD") || upper.equals("BYTE")
                || upper.equals("RESB") || upper.equals("RESW");
    }

    public boolean isRegister(String str) {
        if (str == null) return false;
        String upper = str.toUpperCase();
        return upper.equals("R0") || upper.equals("R1") || upper.equals("R2") || upper.equals("R3")
                || upper.equals("R4") || upper.equals("R5") || upper.equals("R6") || upper.equals("R7") ||
                upper.equals("R8") || upper.equals("R9") || upper.equals("R10") ||
                upper.equals("R11") || upper.equals("R12") || upper.equals("R13") ||
                upper.equals("R14") || upper.equals("R15");
    }





}
