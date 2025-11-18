package lab4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pass {

    public String errorText = "";
    public String nameProg;
    public int startAddress = 0;
    public int endAddress = 0;
    public int countAddress = 0;
    public final int memoryMax = 16777215;

    public List<List<String>> supportTable = new ArrayList<>();
    public List<List<String>> symbolTable = new ArrayList<>();
    public List<List<String>> exitTable = new ArrayList<>();
    public List<String> endSection = new ArrayList<>();

    public int findMark(String mark) {
        for (int i = 0; i < symbolTable.size(); i++) {
            if (mark.equals(symbolTable.get(0).get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void addToBinary(String mark, String OC, String OP1, String OP2) {
        exitTable.get(0).add(mark);
        exitTable.get(1).add(OC);
        exitTable.get(2).add(OP1);
        exitTable.get(3).add(OP2);
    }

    public void addToSymbolTable(String OP1, String OP2, String nameProg, String str) {
        symbolTable.get(0).add(OP1);
        symbolTable.get(1).add(OP2);
        symbolTable.get(2).add(nameProg);
        symbolTable.get(3).add(str);
    }

    public boolean checkMemmory() {
        if (countAddress < 0 || countAddress > memoryMax) {
            errorText = "Ошибка. Выход за пределы доступной памяти";
            return false;
        }
        return true;
    }

    public int findCode(String mark, String[][] operationCode) {
        for (int i = 0; i < operationCode.length; i++) {
            if (mark.toUpperCase().equals(operationCode[i][0])) {
                return i;
            }
        }
        return -1;
    }

    public int findMarkInMarkTable(String mark, String[] addressName, String[] addressTune) {
        if (!symbolTable.isEmpty()) {
            for (int i = 0; i < symbolTable.get(0).size(); i++) {
                if (symbolTable.get(0).get(i).toUpperCase().equals(mark.toUpperCase())) {
                    addressName[0] = symbolTable.get(1).get(i);
                    addressTune[0] = symbolTable.get(2).get(i);
                    return i;
                }
            }
        }
        return -1;
    }

}
