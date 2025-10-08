package lab1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pass {

    public String errorText = "";
    public String nameProg;
    public int startAddress = 0;
    public int endAddress = 0;
    public int countAddress = 0;
    public final int memoryMax = 33554432;

    public List<List<String>> supportTable = new ArrayList<List<String>>();
    public List<List<String>> symbolTable = new ArrayList<List<String>>();

    public int findMark(String mark) {
        for (int i = 0; i < symbolTable.getFirst().size(); i++) {
            if (Objects.equals(mark, symbolTable.getFirst().get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void addToSupportTable(String mark, String OC, String OP1, String OP2) {
        supportTable.get(0).add(mark);
        supportTable.get(1).add(OC);
        supportTable.get(2).add(OP1);
        supportTable.get(3).add(OP2);
    }

    public boolean checkMemory() {
        if (countAddress < 0 || countAddress > memoryMax) {
            errorText = "Ошибка. Выход за пределы доступной памяти\n";
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
}
