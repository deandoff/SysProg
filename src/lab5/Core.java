package lab5;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Core extends Pass {
    private DataCheck dC = new DataCheck();

    public boolean flagEnd;
    public boolean flagStart;

    List<String> extNames = new ArrayList<>();
    List<String> tuneTable = new ArrayList<>();

    public Core() {
        symbolTable.add(new ArrayList<>());
        symbolTable.add(new ArrayList<>());
        symbolTable.add(new ArrayList<>());
        exitTable.add(new ArrayList<>());
        exitTable.add(new ArrayList<>());
        exitTable.add(new ArrayList<>());
        exitTable.add(new ArrayList<>());
    }

    public boolean checkOperationCode(String[][] OCA) {
        int rows = OCA.length;

        for (int i = 0; i < rows; i++) {
            if (OCA[i][0].isEmpty() || OCA[i][1].isEmpty() || OCA[i][2].isEmpty()) {
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
            if (dC.checkNumbers(OCA[i][0])) {
                errorText = "В строке " + (i + 1) + " ошибка. Некорректный МКО";
                return false;
            }
            if (!dC.checkLettersAndNumbers(OCA[i][0])) {
                errorText = "В строке " + (i + 1) + " ошибка. В поле команды недопустимый символ";
                return false;
            }

            if (dC.checkAddress(OCA[i][1])) {
                if (dC.checkRegisters(OCA[i][0]) || dC.checkDirective(OCA[i][0])) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код команды является зарезервированным словом";
                    return false;
                }
                if (Converter.convertHexToDec(OCA[i][1]) > 63) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код команды не должен превышать 3F";
                    return false;
                } else {
                    if (OCA[i][1].length() == 1)
                        OCA[i][1] = Converter.convertToTwoChars(OCA[i][1]);
                }
            } else {
                errorText = "В строке " + (i + 1) + " ошибка. Недопустимые символы в поле кода";
                return false;
            }

            if (dC.checkNumbers(OCA[i][2])) {
                int res = Integer.parseInt(OCA[i][2]);

                if (res <= 0 || res > 4 || res == 3) {
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
                String str1 = String.valueOf(Converter.convertHexToDec(OCA[i][1]));
                String str2 = String.valueOf(Converter.convertHexToDec(OCA[k][1]));
                if (str1.equals(str2)) {
                    errorText = "В строке " + (i + 1) + " ошибка. В поле кода операции найдены совпадения";
                    return false;
                }
            }

        }
        return true;
    }

    public boolean doPass(String[][] sourceCode, String[][] operationCode, JTable symbolDataGrid, JTable binary, int i, JTable tuneDataGrid) {
        int oldAddressCount = 0;
        int countStart = 0;
        boolean flagReplace = false;

        if (flagStart) {
            if (!checkMemmory()) {
                return false;
            }
        }

        String[] output = new String[4];
        if (!dC.checkRow(sourceCode, i, output, nameProg)) {
            errorText = "В строке " + (i + 1) + " синтаксическая ошибка.";
            return false;
        }

        String mark = output[0].toUpperCase();
        String OC = output[1].toUpperCase();
        String OP1 = output[2].toUpperCase();
        String OP2 = output[3].toUpperCase();

        for (int j = 0; j < operationCode.length; j++) {
            if (mark.toUpperCase().equals(operationCode[j][0].toUpperCase())) {
                errorText = "В строке " + (i + 1) + " ошибка. Символическое имя не может совпадать с названием команды";
                return false;
            }
        }

        for (String item : extNames) {
            if (mark.toUpperCase().equals(item.toUpperCase())) {
                errorText = "В строке " + (i + 1) + " ошибка. Символическое имя не может совпадать с названием программы";
                return false;
            }
        }

        String[] addressName = new String[]{""};
        String[] addressTune = new String[]{""};

        int markRow = findMarkInMarkTable(mark, addressName, addressTune);

        if (mark.length() > 0 && !OC.toUpperCase().equals("START")) {
            if (markRow == -1) {
                symbolTable.get(0).add(mark.toUpperCase());
                symbolTable.get(1).add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                symbolTable.get(2).add("");
            } else {
                if (addressName[0].equals("")) {
                    flagReplace = true;
                    oldAddressCount = countAddress;
                } else {
                    errorText = "В строке " + (i + 1) + " ошибка. Повторение символьных имен недопустимо";
                    return false;
                }
            }
        }

        if (dC.checkDirective(OC)) {
            switch (OC) {
                case "START":
                    countStart++;
                    if (i == 0 && !flagStart) {
                        flagStart = true;

                        if (dC.checkAddress(OP1)) {
                            OP1 = OP1.replaceAll("^0+", "");

                            if (OP1.isEmpty()) OP1 = "0";

                            countAddress = Converter.convertHexToDec(OP1);
                            startAddress = countAddress;

                            if (countAddress != 0) {
                                errorText = "В строке " + (i + 1) + " ошибка. Адрес начала программы должен быть равен нулю";
                                return false;
                            }

                            if (countAddress > memoryMax || countAddress < 0) {
                                errorText = "В строке " + (i + 1) + " ошибка. Неправильный адрес загрузки";
                                return false;
                            }

                            if (mark.equals("")) {
                                errorText = "В строке " + (i + 1) + " ошибка. Не задано имя программы";
                                return false;
                            }

                            if (mark.length() > 10) {
                                errorText = "В строке " + (i + 1) + " ошибка. Превышена длина имени программы\n Имя программы должно быть не больше 10 символов";
                                return false;
                            }

                            for (int j = 0; j < operationCode.length; j++) {
                                if (mark.toUpperCase().equals(operationCode[j][0].toUpperCase())) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Имя программы не может совпадать с названием команды";
                                    return false;
                                }
                            }

                            addToBinary("H", mark, Converter.convertToSixChars(OP1), "");
                            nameProg = mark;
                            extNames.add(mark);

                            if (OP2.length() > 0) {
                                errorText = "В строке " + (i + 1) + " второй операнд директивы START не рассматривается. Устраните и повторите заново.";
                                return false;
                            }
                        } else {
                            errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес начала программы";
                            return false;
                        }
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. Повторное использование директивы START";
                        return false;
                    }
                    break;
                case "WORD":
                    if (flagStart) {
                        try {
                            int numb = Integer.parseInt(OP1);
                            if (numb >= 0 && numb <= memoryMax) {
                                if (!addCheckError(i, 3, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), "06", Converter.convertToSixChars(Converter.convertDecToHex(numb)))) {
                                    return false;
                                }

                                if (OP2.length() > 0) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы WORD не рассматривается. Устраните и повторите заново.";
                                    return false;
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Отрицательное число либо превышено максимальное значение числа";
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            errorText = "В строке " + (i + 1) + " ошибка. Неверный формат числа";
                            return false;
                        }
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. Не найдена директива START";
                        return false;
                    }
                    break;
                case "BYTE":
                    if (flagStart) {
                        try {
                            int numb = Integer.parseInt(OP1);
                            if (numb >= 0 && numb <= 255) {
                                if (!addCheckError(i, 1, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), "02", Converter.convertToTwoChars(Converter.convertDecToHex(numb)))) {
                                    return false;
                                }

                                if (OP2.length() > 0) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                    return false;
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Отрицательное число либо превышено максимальное значение числа";
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            String symb = dC.checkAndGetString(OP1);

                            if (!symb.equals("")) {
                                if (symb.length() > 60) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Превышена длина строки";
                                    return false;
                                }

                                boolean[] errorFlags = new boolean[2];
                                String res = checkOP(OP1, errorFlags, -1);
                                boolean er = errorFlags[0];
                                boolean label = errorFlags[1];

                                if (er) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                    return false;
                                }

                                if (!addCheckError(i, symb.length(), "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertToTwoChars(Converter.convertDecToHex(res.length())), res)) {
                                    return false;
                                }

                                if (OP2.length() > 0) {
                                    errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                    return false;
                                }
                            }

                            String symb1 = dC.checkAndGetByteString(OP1);

                            if (!symb1.equals("")) {
                                if (symb1.length() % 2 == 0) {
                                    if (symb1.length() > 60) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Превышена длина строки";
                                        return false;
                                    }

                                    boolean[] errorFlags = new boolean[2];
                                    String res = checkOP(OP1, errorFlags, -1);
                                    boolean er = errorFlags[0];
                                    boolean label = errorFlags[1];

                                    if (er) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                        return false;
                                    }

                                    if (!addCheckError(i, symb1.length() / 2, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertToTwoChars(Converter.convertDecToHex(symb1.length())), res)) {
                                        return false;
                                    }

                                    if (OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы BYTE не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Невозможно преобразовать BYTE нечетное количество символов";
                                    return false;
                                }
                            }

                            if (symb.equals("") && symb1.equals("")) {
                                errorText = "В строке " + (i + 1) + " ошибка. Неверный формат строки " + OP1;
                                return false;
                            }
                        }
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. Не найдена директива START";
                        return false;
                    }
                    break;
                case "RESB":
                    if (flagStart) {
                        try {
                            int numb = Integer.parseInt(OP1);
                            if (numb > 0) {
                                if (countAddress > memoryMax) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Переполнение памяти";
                                    return false;
                                } else {
                                    if (!addCheckError(i, numb, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertDecToHex(numb), "")) {
                                        return false;
                                    }

                                    if (OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд директивы RESB не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Количество байт равно нулю или меньше нуля";
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            errorText = "В строке " + (i + 1) + " ошибка. Невозможно выполнить преобразование " + OP1;
                            return false;
                        }
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. Не найдена директива START";
                        return false;
                    }
                    break;

                case "RESW":
                    if (flagStart) {
                        try {
                            int numb = Integer.parseInt(OP1);
                            if (numb > 0) {
                                if (!addCheckError(i, numb * 3, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertDecToHex(numb * 3), "")) {
                                    return false;
                                }

                                if (OP2.length() > 0) {
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
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. Не найдена директива START";
                        return false;
                    }
                    break;
                case "END":
                    if (mark.length() > 0) {
                        errorText = "В строке " + (i + 1) + " метка. Устраните и повторите заново.";
                        return false;
                    }

                    if (!OP2.equals("")) {
                        errorText = "В строке " + (i + 1) + " второй операнд директивы END не рассматривается. Устраните и повторите заново.";
                        return false;
                    }

                    if (flagStart && !flagEnd) {
                        flagEnd = true;
                        if (OP1.length() == 0) {
                            endAddress = startAddress;
                            addToBinary("E", Converter.convertToSixChars(Converter.convertDecToHex(endAddress)), "", "");

                            int head = findHead("H");
                            if (head > -1) {
                                exitTable.get(3).set(head, Converter.convertToSixChars(Converter.convertDecToHex(countAddress - startAddress)));
                            }

                            if (OP2.length() > 0) {
                                errorText = "В строке " + (i + 1) + " второй операнд директивы END не рассматривается. Устраните и повторите заново.";
                                return false;
                            }

                            break;
                        } else {
                            if (dC.checkAddress(OP1)) {
                                endAddress = Converter.convertHexToDec(OP1);
                                if (endAddress >= startAddress && endAddress <= countAddress) {
                                    int head = findHead("H");
                                    if (head > -1) {
                                        exitTable.get(3).set(head, Converter.convertToSixChars(Converter.convertDecToHex(countAddress - startAddress)));
                                    }

                                    if (flagReplace) {
                                        flagReplace = false;
                                        replaceMark(mark, Converter.convertToSixChars(Converter.convertDecToHex(oldAddressCount)), Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                    }

                                    int[] num = new int[1];
                                    if (!dC.checkEmptyAddress(symbolTable, num)) {
                                        errorText = "Ошибка. Найдено неопределенное символьное имя " + symbolTable.get(0).get(num[0]);
                                        return false;
                                    }

                                    DefaultTableModel model = (DefaultTableModel) symbolDataGrid.getModel();
                                    model.setRowCount(0);

                                    for (int j = 0; j < symbolTable.size(); j++) {
                                        Object[] rowData = {
                                                symbolTable.get(0).get(j),
                                                symbolTable.get(1).get(j),
                                                symbolTable.get(2).get(j),
                                        };
                                        model.addRow(rowData);
                                    }

                                    if(!tuneTable.isEmpty()) {
                                        for (int j = 0; j < tuneTable.size(); j++) {
                                            addToBinary("M", tuneTable.get(j), "", "");
                                        }
                                    }

                                    addToBinary("E", Converter.convertToSixChars(Converter.convertDecToHex(endAddress)), "", "");
                                    break;
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес входа в программу";
                                    return false;
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Неверный адрес входа в программу";
                                return false;
                            }
                        }
                    }
                    break;
            }
        } else {
            if (flagStart) {
                if (OC.length() > 0) {
                    int numb = findCode(OC, operationCode);
                    if (numb > -1) {
                        if (operationCode[numb][2].equals("1")) {
                            boolean[] errorFlags = new boolean[2];
                            String res = checkOP(OP1, errorFlags, -1);
                            boolean er = errorFlags[0];
                            boolean label = errorFlags[1];

                            if (er) {
                                errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                return false;
                            }

                            String str = Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4));

                            if (!addCheckError(i, 1, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertToTwoChars(Converter.convertDecToHex(str.length())), str)) {
                                return false;
                            }

                            if (OP1.length() > 0 || OP2.length() > 0) {
                                errorText = "В строке " + (i + 1) + " операнды не рассматривается в команде " + operationCode[numb][0] + ". Устраните и повторите заново.";
                                return false;
                            }
                        } else if (operationCode[numb][2].equals("2")) {
                            try {
                                int number = Integer.parseInt(OP1);
                                if (number >= 0 && number <= 255) {
                                    boolean[] errorFlags = new boolean[2];
                                    String res = checkOP(OP1, errorFlags, -1);
                                    boolean er = errorFlags[0];
                                    boolean label = errorFlags[1];

                                    if (er) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                        return false;
                                    }

                                    String str = Converter.convertToTwoChars(Converter.convertDecToHex(Integer.parseInt(operationCode[numb][2]) + res.length()));

                                    if (!addCheckError(i, 2, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), str,
                                            Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4)) + Converter.convertToTwoChars(Converter.convertDecToHex(number)))) {
                                        return false;
                                    }

                                    if (OP2.length() > 0) {
                                        errorText = "В строке " + (i + 1) + " второй операнд не рассматривается в команде " + operationCode[numb][0] + ". Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Отрицательное число либо превышено максимальное значение числа";
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                if (dC.checkRegisters(OP1) && dC.checkRegisters(OP2)) {
                                    boolean[] errorFlags1 = new boolean[2];
                                    String res1 = checkOP(OP1, errorFlags1, -1);
                                    boolean er1 = errorFlags1[0];
                                    boolean label1 = errorFlags1[1];

                                    if (er1) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                        return false;
                                    }

                                    boolean[] errorFlags2 = new boolean[2];
                                    String res2 = checkOP(OP2, errorFlags2, -1);
                                    boolean er2 = errorFlags2[0];
                                    boolean label2 = errorFlags2[1];

                                    if (er2) {
                                        errorText = "В строке " + (i + 1) + " ошибка. Ошибка в операнде, код отсутствует в ТСИ";
                                        return false;
                                    }

                                    String str = Converter.convertToTwoChars(Converter.convertDecToHex(2 + res1.length() + res2.length()));

                                    if (!addCheckError(i, 2, "T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), str,
                                            Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4)) + res1 + res2)) {
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке " + (i + 1) + " ошибка. Ошибка в команде " + operationCode[numb][0];
                                    return false;
                                }
                            }
                        } else if (operationCode[numb][2].equals("4")) {
                            if (countAddress + 4 > memoryMax) {
                                errorText = "В строке " + (i + 1) + " ошибка. Произошло переполнение";
                                return false;
                            }

                            int adrType;
                            int tpp;

                            if (OP1.length() > 0) {
                                if (OP1.charAt(0) == '[' && OP1.charAt(OP1.length() - 1) == ']') {
                                    OP1 = OP1.substring(1, OP1.length() - 1);
                                    if (OP1.length() > 0) {
                                        if (dC.checkLettersAndNumbers(OP1) && dC.checkLetters(Character.toString(OP1.charAt(0)))) {
                                            // все хорошо, ничего не делаем
                                        } else {
                                            errorText = "В строке " + (i + 1) + " ошибка. Ошибка в символическом имени";
                                            return false;
                                        }
                                    } else {
                                        errorText = "В строке " + (i + 1) + " ошибка. Не все символические имена имеют адрес";
                                        return false;
                                    }
                                    adrType = Converter.convertHexToDec(operationCode[numb][1]) * 4 + 2;
                                    tpp = 2;
                                } else {
                                    if (dC.checkLettersAndNumbers(OP1) && dC.checkLetters(Character.toString(OP1.charAt(0)))) {
                                        tuneTable.add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                    } else {
                                        errorText = "В строке " + (i + 1) + " ошибка. Для данного типа адресации операнд должен быть меткой";
                                        return false;
                                    }

                                    adrType = Converter.convertHexToDec(operationCode[numb][1]) * 4 + 1;
                                    tpp = 1;
                                }
                            } else {
                                errorText = "В строке " + (i + 1) + " ошибка. Не найден операнд";
                                return false;
                            }

                            for (int j = 0; j < operationCode.length - 1; j++) {
                                if (OP1.toUpperCase().equals(operationCode[j][0].toUpperCase())) {
                                    errorText = "В строке " + (j + 1) + " ошибка. Символическое имя не может совпадать с названием команды";
                                    return false;
                                }
                            }

                            for (String item : extNames) {
                                if (OP1.toUpperCase().equals(item.toUpperCase())) {
                                    errorText = "В строке " + (i + 1) + " ошибка. Символическое имя не может совпадать с названием программы";
                                    return false;
                                }
                            }

                            if (dC.checkDirective(OP1) || dC.checkRegisters(OP1)) {
                                errorText = "В строке " + (i + 1) + " ошибка. Символическое имя не может совпадать с названием директивы/регистра";
                                return false;
                            }

                            String[] opAdr = new String[]{""};
                            String[] tuneAdr = new String[]{""};
                            int finded = findMarkInMarkTable(OP1, opAdr, tuneAdr);

                            if (finded > -1) {
                                if (!Objects.equals(opAdr[0], "")) {
                                    if (tpp == 1) {
                                        String tmp = Converter.convertToTwoChars(Converter.convertDecToHex(adrType)) + opAdr[0];
                                        addToBinary("T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)),
                                                Converter.convertToTwoChars(Converter.convertDecToHex(tmp.length())), tmp);
                                    } else if (tpp == 2) {
                                        if (!checkMemmory()) {
                                            return false;
                                        }
                                        String str = Converter.convertToTwoChars(Converter.convertDecToHex(adrType)) + Converter.convertSubHex(symbolTable.get(1).get(finded), Converter.convertToSixChars(Converter.convertDecToHex(countAddress + 4)));
                                        addToBinary("T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), Converter.convertToTwoChars(Converter.convertDecToHex(str.length())), str);
                                    }
                                }
                                else {
                                    symbolTable.get(0).add(finded + 1, OP1.toUpperCase());
                                    symbolTable.get(1).add(finded + 1, "");
                                    symbolTable.get(2).add(finded + 1, Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                    addToBinary("T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), "",
                                            Converter.convertToTwoChars(Converter.convertDecToHex(adrType)) + "#" + OP1 + "#");
                                }
                            } else {
                                symbolTable.get(0).add(OP1.toUpperCase());
                                symbolTable.get(1).add("");
                                symbolTable.get(2).add(Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
                                addToBinary("T", Converter.convertToSixChars(Converter.convertDecToHex(countAddress)), "",
                                        Converter.convertToTwoChars(Converter.convertDecToHex(adrType)) + "#" + OP1 + "#");
                            }

                            countAddress += 4;

                            if (!checkMemmory()) {
                                return false;
                            }

                            if (OP2.length() > 0) {
                                errorText = "В строке " + (i + 1) + " второй операнд не рассматривается в команде " + operationCode[numb][0] + ". Устраните и повторите заново.";
                                return false;
                            }
                        } else {
                            errorText = "В строке " + (i + 1) + " размер команды больше установленного";
                            return false;
                        }
                    } else {
                        errorText = "В строке " + (i + 1) + " ошибка. В ТКО не найдено " + OC;
                        return false;
                    }
                } else {
                    errorText = "В строке " + (i + 1) + " ошибка. Синтаксическая ошибка";
                    return false;
                }
            } else {
                errorText = "В строке " + (i + 1) + " ошибка. Не найдена директива START";
                return false;
            }
        }
        if (flagReplace) {
            replaceMark(mark, Converter.convertToSixChars(Converter.convertDecToHex(oldAddressCount)), Converter.convertToSixChars(Converter.convertDecToHex(countAddress)));
        }

        DefaultTableModel binaryModel = (DefaultTableModel) binary.getModel();
        binaryModel.setRowCount(0);

        DefaultTableModel tuneModel = (DefaultTableModel) tuneDataGrid.getModel();
        tuneModel.setRowCount(0);

        if (tuneTable.size() > 0) {
            for (int j = 0; j < tuneTable.size(); j++) {
                tuneModel.addRow(new Object[]{
                        tuneTable.get(j)
                });
            }
        }

        if (exitTable.get(0).size() > 0) {
            for (int j = 0; j < exitTable.get(0).size(); j++) {
                binaryModel.addRow(new Object[]{
                        exitTable.get(0).get(j),
                        exitTable.get(1).get(j),
                        exitTable.get(2).get(j),
                        exitTable.get(3).get(j)
                });
            }
        }

        DefaultTableModel symbolModel = (DefaultTableModel) symbolDataGrid.getModel();
        symbolModel.setRowCount(0);

        for (int j = 0; j < symbolTable.get(1).size(); j++) {
            symbolModel.addRow(new Object[]{
                    symbolTable.get(0).get(j),
                    symbolTable.get(1).get(j),
                    symbolTable.get(2).get(j)
            });
        }

        return true;
    }


    private int replaceMark(String mark, String markAdr, String currentAdr) {
        List<List<String>> marks = new ArrayList<>();

        marks.add(new ArrayList<>());
        marks.add(new ArrayList<>());
        marks.add(new ArrayList<>());

        List<String> adrCounter = new ArrayList<>();

        for (int j = 0; j < exitTable.get(0).size(); j++) {
            adrCounter.add(exitTable.get(1).get(j));
        }
        adrCounter.add(currentAdr);

        String n1 = "";
        String n2 = "";

        for (int i = 0; i < symbolTable.get(0).size(); i++) {
            if (mark.equals(symbolTable.get(0).get(i)) && symbolTable.get(1).get(i).equals("")) {
                symbolTable.get(1).set(i, markAdr);

                n1 = symbolTable.get(0).get(i);
                n2 = symbolTable.get(1).get(i);

                for (int j = 0; j < exitTable.get(0).size(); j++) {
                    if (symbolTable.get(2).get(i).equals(exitTable.get(1).get(j))) {
                        if (exitTable.get(3).get(j).length() > 0) {
                            String type = exitTable.get(3).get(j).substring(0, 2);
                            int typeOfAdr = (byte) Converter.convertHexToDec(type) & 0x03;

                            int index = exitTable.get(3).get(j).indexOf("#" + mark + "#");
                            if (index > -1) {
                                switch (typeOfAdr) {
                                    case 1:
                                        exitTable.get(3).set(j, exitTable.get(3).get(j).replace("#" + mark + "#", markAdr));
                                        break;
                                    case 2:
                                        exitTable.get(3).set(j, exitTable.get(3).get(j).replace("#" + mark + "#",
                                                Converter.convertSubHex(n2, adrCounter.get(j + 1))));
                                        break;
                                }

                                exitTable.get(2).set(j, Converter.convertToTwoChars(Converter.convertDecToHex(exitTable.get(3).get(j).length())));
                            }
                        }
                    }
                }
            } else {
                marks.get(0).add(symbolTable.get(0).get(i));
                marks.get(1).add(symbolTable.get(1).get(i));
                marks.get(2).add(symbolTable.get(2).get(i));
            }
        }

        symbolTable = marks;
        symbolTable.get(0).add(0, n1);
        symbolTable.get(1).add(0, n2);
        symbolTable.get(2).add(0, "");

        return -1;
    }

    private int findHead(String head) {
        if (exitTable.get(0).size() > 0) {
            for (int i = 0; i < exitTable.get(0).size(); i++) {
                if (head.equals(exitTable.get(0).get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean addCheckError(int i, int numbToAdd, String type, String count, String numb, String OP1) {
        if (countAddress + numbToAdd > memoryMax) {
            errorText = "В строке " + (i + 1) + " ошибка. Произошло переполнение";
            return false;
        }

        addToBinary(type, count, numb, OP1);

        countAddress += numbToAdd;

        if (!checkMemmory()) {
            return false;
        }

        return true;
    }

    public String checkOP(String OP, boolean[] errorFlags, int ind) {
        errorFlags[0] = false; // er
        errorFlags[1] = false; // operandLabel

        if (!OP.equals("")) {
            int reg = dC.getRegisters(OP);
            if (reg > -1) {
                return Converter.convertDecToHex(reg);
            } else if (dC.checkNumbers(OP)) {
                return Converter.convertDecToHex(Integer.parseInt(OP));
            } else {
                String str = dC.checkAndGetString(OP);
                if (!str.equals("")) {
                    return Converter.convertASCII(str);
                }

                str = dC.checkAndGetByteString(OP);
                if (!str.equals("")) {
                    return str;
                }

                errorFlags[0] = true; // er = true
            }
        }
        return "";
    }

    public boolean isFlagEnd() {
        return flagEnd;
    }

    public String getErrorText() {
        return errorText;
    }
}