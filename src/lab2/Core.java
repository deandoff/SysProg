package lab2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Core extends Pass {

    private DataCheck dC = new DataCheck();

    public boolean checkOperationCode(String[][] OCA) {
        int rows = OCA[0].length;

        for (int i = 0; i < rows; i++) {
            if (OCA[i][0].equals("") || OCA[i][1].equals("") || OCA[i][2].equals("")) {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустима пустая ячейка в ТКО";
                return false;
            }

            if (OCA[i][0].length() > 6 || OCA[i][1].length() > 2 || OCA[i][2].length() > 1) {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустимый размер строки в ТКО. Команда - от 1 до 6. Код - от 1 до 2. Длина - не более одного";
                return false;
            }

            if (!dC.checkLettersAndNumbers(OCA[i][0])) {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустимый символ в поле команды";
                return false;
            }

            if (dC.checkLettersAndNumbers(OCA[i][1])) {
                if (dC.checkRegisters(OCA[i][0]) || dC.checkDirective(OCA[i][0])) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код команды является зарезервированным словом";
                    return false;
                }

                if (Converter.convertHexToDec(OCA[i][1]) > 63) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код команды не должен превышать 3F";
                    return false;
                } else {
                    if (OCA[i][1].length() == 1) {
                        OCA[i][1] = Converter.convertToTwoChars(OCA[i][1]);
                    }
                }
            } else {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустимые символы в поле кода";
                return false;
            }

            if (dC.checkNumbers(OCA[i][2])) {
                int res = Integer.parseInt(OCA[i][2]);

                if ((res <= 0) || (res > 4) || (res == 3)) {
                    errorText = "В строке " + (i + 1) + " ошибка. Недопустимый размер команды. Должен быть 1, 2 или 4";
                    return false;
                }
            } else {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустимые символы в поле размера операции";
                return false;
            }

            for (int k = i + 1; k < rows; k++) {
                String str1 = OCA[i][0];
                String str2 = OCA[k][0];
                if (str1.equals(str2)) {
                    errorText = "В строке " + (i + 1) + " ошибка. В поле команда найдены совпадения";
                    return false;
                }
            }

            for (int k = i + 1; k < rows; k++) {
                String str1 = Integer.toString(Converter.convertHexToDec(OCA[i][1]));
                String str2 = Integer.toString(Converter.convertHexToDec(OCA[k][1]));
                if (str1.equals(str2)) {
                    errorText = "В строке " + (i + 1) + " ошибка. В поле кода операции найдены совпадения";
                    return false;
                }
            }
        }

        return true;
    }

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

        symbolTable.add(new ArrayList<String>());
        symbolTable.add(new ArrayList<String>());

        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());
        supportTable.add(new ArrayList<String>());

        int numberRows = sourceCode.length;

        boolean flagStart = false;
        boolean flagEnd = false;

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

            String[] rowData = new String[4];

            boolean rowOk = dC.checkRow(sourceCode, i, rowData, nameProg);
            System.out.printf("Line %d: checkRow returns %b, rowData=%s%n", i+1, rowOk, Arrays.toString(rowData));

            if (!dC.checkRow(sourceCode, i, rowData, nameProg) && i == 0) {
                errorText = "В строке"+ (i + 1) +" синтаксическая ошибка.";
                return false;
            }

            System.out.println(Arrays.toString(rowData));

            String mark = rowData[0];
            String OC = rowData[1];
            String OP1 = rowData[2];
            String OP2 = rowData[3];

            if (!mark.isEmpty()) {
                if (!mark.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
                    errorText = "В строке " + (i + 1) + " ошибка: недопустимые символы в имени метки '" + mark + "'";
                    return false;
                }
            }

// --- Проверка повторяющихся меток ---
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

            // Проверка на дубликаты меток
            if (findMark(mark) != -1) {
                errorText = "В строке " + (i + 1) + " ошибка. Найдена уже существующая метка " + mark;
                return false;
            }
// --- Проверка пустых операндов ---
            if (OP1.contains("[]") || OP2.contains("[]")) {
                errorText = "В строке " + (i + 1) + " ошибка: пустой операнд '[]'";
                return false;
            }

// --- Проверка уникальности метки ---
            if (!mark.isEmpty() && findMark(mark) != -1) {
                errorText = "В строке " + (i + 1) + " ошибка: метка '" + mark + "' уже существует";
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

            if (findMark(mark) != -1) {
                errorText = "В строке"+ (i + 1) +" ошибка. Найдена уже существующая метка " + mark;
                return false;
            } else {

                if (!Objects.equals(mark, "") && flagStart) {
                    symbolTable.get(0).add(mark);
                    symbolTable.get(1).add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                }

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

                                OP1 = OP1.replaceFirst("^0+", "");
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
                                // START уже был использован — выдаём корректное сообщение
                                errorText = "В строке " + (i + 1) + " ошибка. Повторное использование директивы START.";
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
        if (!flagEnd) {
            errorText = "Не найдена точка входа в программу";
            return false;
        }
        for (int i = 0; i < supportTable.get(0).size(); i++)
        {
            DefaultTableModel supportModel = (DefaultTableModel) supportTableJT.getModel();
            supportModel.addRow(new Object[]{supportTable.get(0).get(i), supportTable.get(1).get(i), supportTable.get(2).get(i), supportTable.get(3).get(i)});
        }

        for (int i = 0; i < symbolTable.get(1).size(); i++)
        {
            DefaultTableModel symbolModel = (DefaultTableModel) symbolTableJT.getModel();
            symbolModel.addRow(new Object[]{symbolTable.get(0).get(i), symbolTable.get(1).get(i)});
        }

        return true;
    }

    public boolean doSecondPass(JTextArea BC, JTable settingTableJT, int typeAdr) {
        errorText = "";
        List<String> settingTable = new ArrayList<>();

        for (int i = 0; i < supportTable.get(0).size(); i++) {
            String address = supportTable.get(0).get(i);
            String OC = supportTable.get(1).get(i);
            String OP1 = supportTable.get(2).get(i);
            String OP2 = supportTable.get(3).get(i);

            // --- первая строка: директива START ---
            if (i == 0) {
                BC.setText(Converter.convertToBinaryCodeSTART(
                        address, OP1, Integer.toString(countAddress - startAddress)) + "\r\n");
                continue;
            }

            // --- обработка первого операнда ---
            boolean[] outFlags1 = new boolean[2];
            String[] outAddress = new String[1];
            String res = checkOP(OP1, outFlags1, outAddress, i);
            boolean error1 = outFlags1[0];
            boolean flagMark = outFlags1[1];
            String tuneAddress = outAddress[0];

            if (error1) {
                errorText = "В строке " + (i + 1) + " ошибка. Код операнда отсутствует в ТСИ.";
                BC.setText("");
                break;
            }

            settingTable(tuneAddress, settingTable);

            boolean[] outFlags2 = new boolean[2];
            String[] outAddress2 = new String[1];
            String ress = checkOP(OP2, outFlags2, outAddress2, i);
            boolean error2 = outFlags2[0];
            tuneAddress = outAddress2[0];

            if (error2) {
                errorText = "В строке " + (i + 1) + " ошибка. Код операнда отсутствует в ТСИ.";
                BC.setText("");
                break;
            }

            settingTable(tuneAddress, settingTable);

            // --- выравнивание шестнадцатеричных строк ---
            if (res != null && !res.isEmpty()) res = padHexEven(res);
            if (ress != null && !ress.isEmpty()) ress = padHexEven(ress);

            // --- вычисление кода операции с учётом типа адресации ---
            int opcodeBase = 0;
            int finalOpcode = 0;
            int offset = 0;

            // Определяем базовый код операции (для директив = 0)
            if (!dC.checkDirective(OC)) {
                try {
                    opcodeBase = Converter.convertHexToDec(OC);
                } catch (Exception e) {
                    opcodeBase = 0;
                }
            }

            if (typeAdr == 0) {             // Прямая
                finalOpcode = opcodeBase;
            } else if (typeAdr == 1) {      // Относительная
                finalOpcode = opcodeBase + 1;
            } else if (typeAdr == 2) {      // Смешанная
                if ((!OP1.isEmpty() && OP1.startsWith("[") && OP1.endsWith("]")) ||
                        (!OP2.isEmpty() && OP2.startsWith("[") && OP2.endsWith("]"))) {
                    finalOpcode = opcodeBase + 1;
                } else {
                    finalOpcode = opcodeBase;
                }
            }

            // --- обработка директив ---
            if (dC.checkDirective(OC)) {
                switch (OC) {
                    case "RESB":
                        BC.append(Converter.convertToBinaryCode(
                                address,
                                Converter.convertToTwoChars(Converter.convertDecToHex(finalOpcode)),
                                res, "", "") + "\r\n");
                        continue;

                    case "RESW":
                        BC.append(Converter.convertToBinaryCode(
                                address,
                                Converter.convertToTwoChars(Converter.convertDecToHex(finalOpcode)),
                                Converter.convertToTwoChars(
                                        Converter.convertDecToHex(Integer.parseInt(OP1) * 3)),
                                "", "") + "\r\n");
                        continue;

                    case "BYTE":
                        BC.append(Converter.convertToBinaryCode(
                                address,
                                Converter.convertToTwoChars(Converter.convertDecToHex(finalOpcode)),
                                Converter.convertToTwoChars(
                                        Converter.convertDecToHex(res.length() + ress.length())),
                                res, ress) + "\r\n");
                        continue;

                    case "WORD":
                        BC.append(Converter.convertToBinaryCode(
                                address,
                                Converter.convertToTwoChars(Converter.convertDecToHex(finalOpcode)),
                                Converter.convertToTwoChars(
                                        Converter.convertDecToHex(
                                                Converter.convertToSixChars(res).length() + ress.length())),
                                Converter.convertToSixChars(res), ress) + "\r\n");
                        continue;
                }
            }

            // --- обработка инструкций ---
            else {
                // Проверка корректности для относительной адресации
                if (offset == 2 && !flagMark) {
                    errorText = "В строке " + (i + 1) + " ошибка. Для относительной адресации операнд должен быть меткой";
                    BC.setText("");
                    return false;
                }

                // Проверка для смешанного типа
                if (offset == 2 && !ress.isEmpty()) {
                    errorText = "В строке " + (i + 1) + " ошибка. Относительная адресация поддерживает только один операнд";
                    BC.setText("");
                    return false;
                }

                // Финальное добавление команды в бинарный код
                String opcodeHex = Converter.convertToTwoChars(Converter.convertDecToHex(finalOpcode));

                BC.append(Converter.convertToBinaryCode(
                        address,
                        opcodeHex,
                        Converter.convertToTwoChars(
                                Converter.convertDecToHex(opcodeHex.length() + res.length() + ress.length())),
                        res, ress) + "\r\n");
            }

            // --- обновление таблицы настройки в интерфейсе ---
            if (!settingTable.isEmpty()) {
                DefaultTableModel model = (DefaultTableModel) settingTableJT.getModel();
                model.setRowCount(0);
                for (String s : settingTable) {
                    model.addRow(new Object[]{s});
                }
            }
        }
        
        for (String s : settingTable) {
            BC.append(Converter.convertToBinaryCodeSetting(s) + "\r\n");
        }

        // --- добавление конца программы ---
        BC.append(Converter.convertToBinaryCodeEND(
                Converter.convertToSixChars(
                        Converter.convertDecToHex(endAddress))) + "\r\n");

        if (!errorText.isEmpty())
            BC.setText("");

        return true;
    }



    public String checkOP(String OP, boolean[] outFlags, String[] outAddress, int ind) {
        String res = "";

        boolean er = false;
        boolean flag = false;
        String address = "";

        if (!Objects.equals(OP, "")) {
            int n = findMark(OP);
            if (n > -1) {
                flag = true;
                address = supportTable.get(0).get(ind);
                res = symbolTable.get(1).get(n);
            }
            else if (OP.charAt(0) == '[' && OP.charAt(OP.length() - 1) == ']') {
                String temp = OP.substring(1, OP.length() - 1);
                n = findMark(temp);
                if (n > -1) {
                    flag = true;
                    res = Converter.convertSubHex(symbolTable.get(1).get(n), supportTable.get(0).get(ind + 1));
                } else {
                    er = true;
                }
            }
            else {
                int reg = dC.getRegisters(OP);
                int maxValue = (int) Math.pow(2, 31) - 1;

                if (reg > -1) {
                    res = Converter.convertDecToHex(reg);
                }
                else if (dC.checkNumbers(OP)) {
                    double val = Double.parseDouble(OP);
                    if (val > maxValue || val < (maxValue * -1) + 1) {
                        er = true;
                    } else {
                        res = Converter.convertDecToHex((int) val);
                    }
                }
                else {
                    String str = dC.checkAndGetString(OP);
                    if (!str.isEmpty()) {
                        res = Converter.convertASCII(str);
                    } else {
                        str = dC.checkAndGetByteString(OP);
                        if (!str.isEmpty()) {
                            res = str;
                        } else {
                            er = true;
                        }
                    }
                }
            }
        }

        outFlags[0] = er;
        outFlags[1] = flag;
        outAddress[0] = address;

        System.out.printf("checkOP('%s') -> er=%b, flag=%b, address=%s, result='%s'%n",
                OP, er, flag, address, res);

        return res;
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

    public boolean settingTable(String adr, List<String> settingTable) {
        if (adr != null && !adr.isEmpty()) {
            if (settingTable.indexOf(adr) > 0) {
                return false;
            } else {
                settingTable.add(adr);
            }
        }
        return true;
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
