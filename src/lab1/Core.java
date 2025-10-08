package lab1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean doFirstPass(String[][] sourceCode, String[][] operationCode, JTable supportTableJT, JTable symbolTableJT) {
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

            if (!dC.checkRow(sourceCode, i, rowData, nameProg)) {
                errorText = "В строке"+ (i + 1) +" синтаксическая ошибка.";
                return false;
            }

            System.out.println(Arrays.toString(rowData));

            String mark = rowData[0];
            String OC = rowData[1];
            String OP1 = rowData[2];
            String OP2 = rowData[3];

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
                            if (i == 0 && !flagStart) {
                                flagStart = true;
                                if (dC.checkAddress(OP1)) {
                                    OP1 = OP1.replaceFirst("^0+", "");
                                    countAddress = Converter.convertHexToDec(OP1);

                                    System.out.println(countAddress);

                                    startAddress = countAddress;

                                    if (countAddress == 0) {
                                        errorText = "В строке" + (i + 1) + " ошибка. Адрес начала программы не может быть равен нулю";
                                        return false;
                                    }

                                    if (countAddress > memoryMax || countAddress < 0) {
                                        errorText = "В строке" + (i + 1) + " ошибка. Неправильный адрес загрузки";
                                        return false;
                                    }

                                    if (mark == "") {
                                        errorText = "В строке" + (i + 1) + " ошибка. Не задано имя программы";
                                        return false;
                                    }

                                    if (mark.length() > 10) {
                                        errorText = "В строке" + (i + 1) + " ошибка. Превышена длина имени программы\\n Имя программы должно быть не больше 10 символов";
                                        return false;
                                    }

                                    addToSupportTable(mark, OC, Converter.convertToSixChars(OP1), "");
                                    nameProg = mark;

                                    if (OP2.length() > 0) {
                                        errorText = "В строке" + (i + 1) + " второй операнд директивы START не рассматривается. Устраните и повторите заново.";
                                        return false;
                                    }
                                } else {
                                    errorText = "В строке" + (i + 1) + " ошибка. Повторное использование директивы START";
                                    return false;
                                }
                                break;
                            }

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
                            if (operationCode[numb][2] == "1") {
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
                                if (!addCheckError(i,4,Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertHexToDec(operationCode[numb][1]) * 4 + 1)), OP1, OP2))
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

    public boolean doSecondPass(JTextArea BC) {
        errorText = "";

        for (int i = 0; i < supportTable.get(0).size(); i++) {
            String address = supportTable.get(0).get(i);
            String OC = supportTable.get(1).get(i);
            String OP1 = supportTable.get(2).get(i);
            String OP2 = supportTable.get(3).get(i);

            if (i == 0) {
                BC.setText(Converter.convertToBinaryCodeSTART(address, OP1, Integer.toString(countAddress - startAddress)) + "\r\n");
            } else {
                boolean[] outFlags1 = new boolean[2];
                String res = checkOP(OP1, outFlags1);
                boolean error = outFlags1[0];
                boolean flagMark = outFlags1[1];

                if (error) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код операнда отсутствует в ТСИ.";
                    BC.setText("");
                    break;
                }

                boolean[] outFlags2 = new boolean[2];
                String ress = checkOP(OP2, outFlags2);
                error = outFlags2[0];

                if (error) {
                    errorText = "В строке " + (i + 1) + " ошибка. Код операнда отсутствует в ТСИ.";
                    BC.setText("");
                    break;
                }

                if (dC.checkDirective(OC)) {
                    if (OC.equals("RESB")) {
                        BC.setText(BC.getText() + Converter.convertToBinaryCode(address, "", res, "", "") + "\r\n");
                        continue;
                    } else if (OC.equals("RESW")) {
                        BC.setText(BC.getText() + Converter.convertToBinaryCode(address, "", Converter.convertToTwoChars(Converter.convertDecToHex(Integer.parseInt(OP1) * 3)), "", "") + "\r\n");
                        continue;
                    } else if (OC.equals("BYTE")) {
                        BC.setText(BC.getText() + Converter.convertToBinaryCode(address, "", Converter.convertToTwoChars(Converter.convertDecToHex(res.length() + ress.length())), res, ress) + "\r\n");
                        continue;
                    } else if (OC.equals("WORD")) {
                        BC.setText(BC.getText() + Converter.convertToBinaryCode(address, "", Converter.convertToTwoChars(Converter.convertDecToHex(Converter.convertToSixChars(res).length() + ress.length())), Converter.convertToSixChars(res), ress) + "\r\n");
                        continue;
                    }
                } else {
                    int type = (byte) Converter.convertHexToDec(OC) & 0x03;
                    if (type == 1) {
                        if (!flagMark) {
                            errorText = "В строке " + (i + 1) + " ошибка. Для данного типа адресации операнд должен быть меткой";
                            BC.setText("");
                            return false;
                        }
                        if (!ress.equals("")) {
                            errorText = "В строке " + (i + 1) + " ошибка. Данный тип адрессации поддерживает один операнд";
                            BC.setText("");
                            return false;
                        }
                    }

                    BC.setText(BC.getText() + Converter.convertToBinaryCode(address, OC,
                            Converter.convertToTwoChars(Converter.convertDecToHex(OC.length() + res.length() + ress.length())), res, ress) + "\r\n");
                }
            }
        }

        BC.setText(BC.getText() + Converter.convertToBinaryCodeEND(Converter.convertToSixChars(Converter.convertDecToHex(endAddress))) + "\r\n");

        if (!errorText.equals(""))
            BC.setText("");

        return true;
    }

    public String checkOP(String OP, boolean[] outFlags) {
        String res = "";

        boolean er = false;
        boolean flag = false;

        if (!OP.isEmpty()) {
            int n = findMark(OP);
            if (n > -1) {
                flag = true;
                outFlags[0] = false;  // er
                outFlags[1] = true;   // flagMark
                return symbolTable.get(1).get(n);
            } else {
                int reg = dC.getRegisters(OP);
                if (reg > -1) {
                    outFlags[0] = false;
                    outFlags[1] = false;
                    return Converter.convertDecToHex(reg);
                } else if (dC.checkNumbers(OP)) {
                    outFlags[0] = false;
                    outFlags[1] = false;
                    return Converter.convertDecToHex(Integer.parseInt(OP));
                } else {
                    String str = dC.checkAndGetString(OP);
                    if (!str.isEmpty()) {
                        outFlags[0] = false;
                        outFlags[1] = false;
                        return Converter.convertASCII(str);
                    }

                    str = dC.checkAndGetByteString(OP);
                    if (!str.isEmpty()) {
                        outFlags[0] = false;
                        outFlags[1] = false;
                        return str;
                    }

                    // если ни один случай не подошёл — ошибка
                    er = true;
                }
            }
        }

        outFlags[0] = er;
        outFlags[1] = flag;

        System.out.printf("checkOP('%s') -> er=%b flagMark=%b result='%s'%n",
                OP, outFlags[0], outFlags[1], res);

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






}
