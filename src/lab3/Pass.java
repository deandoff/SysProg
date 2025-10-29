package lab3;

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

    public List<List<String>> supportTable = new ArrayList<List<String>>();
    public List<List<String>> symbolTable = new ArrayList<List<String>>();
    public List<String> endSection = new ArrayList<>();

    public int findMark(String mark) {
        for (int i = 0; i < symbolTable.getFirst().size(); i++) {
            if (Objects.equals(mark, symbolTable.getFirst().get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void addToSymbolTable(String OP1, String OP2, String nameProg, String str) {
        symbolTable.get(0).add(OP1);
        symbolTable.get(1).add(OP2);
        symbolTable.get(2).add(nameProg);
        symbolTable.get(3).add(str);
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

    public int findMarkInMarkTable(String mark, String[] strch, String currentCsectName) {
        int size = symbolTable.get(0).size();

        for (int i = 0; i < size; i++) {
            String symName = symbolTable.get(0).get(i);
            String symAddr = symbolTable.get(1).size() > i ? symbolTable.get(1).get(i) : "";
            String symCsect = symbolTable.get(2).size() > i ? symbolTable.get(2).get(i) : "";
            String symFlag = symbolTable.get(3).size() > i ? symbolTable.get(3).get(i) : "";

            if (currentCsectName.isEmpty()) {
                if (mark.equalsIgnoreCase(symName) && !symAddr.isEmpty()) {
                    strch[0] = "Er";
                    return i;
                } else if (mark.equalsIgnoreCase(symName) && symAddr.isEmpty() && !"ВНС".equals(symFlag)) {
                    strch[0] = "mk";
                    return i;
                } else if (mark.equalsIgnoreCase(symName) && symAddr.isEmpty() && "ВНС".equals(symFlag)) {
                    strch[0] = "Er";
                    return i;
                }
            } else {
                if (mark.equalsIgnoreCase(symName) && !symAddr.isEmpty() && currentCsectName.equalsIgnoreCase(symCsect)) {
                    strch[0] = "Er";
                    return i;
                } else if (mark.equalsIgnoreCase(symName) && symAddr.isEmpty()
                        && !"ВНС".equals(symFlag) && currentCsectName.equalsIgnoreCase(symCsect)) {
                    strch[0] = "mk";
                    return i;
                } else if (mark.equalsIgnoreCase(symName) && symAddr.isEmpty()
                        && "ВНС".equals(symFlag) && currentCsectName.equalsIgnoreCase(symCsect)) {
                    strch[0] = "Er";
                    return i;
                }
            }
        }
        return -1;
    }

    /** Добавляет символ в таблицу, только если ещё не существует в той же секции */
    void addSymbolIfNotExists(String mark, int addressDec, String csectName, String flagIfAny) {
        if (mark == null || mark.isEmpty()) return;
        String markUp = mark.toUpperCase();
        String csectUp = (csectName == null) ? "" : csectName.toUpperCase();
        // Проверяем — уже есть ли такое имя в той же секции
        for (int k = 0; k < symbolTable.get(0).size(); k++) {
            String existingName = symbolTable.get(0).get(k);
            String existingCsect = symbolTable.get(2).get(k);
            if (existingName.equalsIgnoreCase(markUp) && existingCsect.equalsIgnoreCase(csectUp)) {
                // Уже есть — если нужно, обновим адрес или флаг в особых случаях:
                if (flagIfAny != null && !flagIfAny.isEmpty()) {
                    symbolTable.get(3).set(k, flagIfAny); // обновляем флаг
                }
                // если адрес пустой и сейчас у нас задан адрес — обновим
                if ((symbolTable.get(1).get(k) == null || symbolTable.get(1).get(k).isEmpty())
                        && addressDec >= 0) {
                    symbolTable.get(1).set(k, Converter.convertToSixChars(Converter.convertDecToHex(addressDec)));
                }
                return; // не добавляем дубликат
            }
        }
        // Не найден — добавляем новую запись (адрес в шестизначном виде или "")
        String addrHex = (addressDec >= 0) ? Converter.convertToSixChars(Converter.convertDecToHex(addressDec)) : "";
        symbolTable.get(0).add(markUp);
        symbolTable.get(1).add(addrHex);
        symbolTable.get(2).add(csectUp);
        symbolTable.get(3).add(flagIfAny == null ? "" : flagIfAny);
    }

}
