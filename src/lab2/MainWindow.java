package lab2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;


public class MainWindow extends JFrame {
    private final JTextArea sourceCodeTextBox;
    private final JTable operationCodeTable;
    private final JTable symbolTable;
    private final JTable supportTable;
    private final JTextArea firstPassErrorTextBox;
    private final JTextArea secondPassErrorTextBox;
    private final JTextArea binaryCodeTextBox;
    private final JTable settingTable;
    private final JComboBox<String> addressingModeComboBox;
    private int typeAdr = 0;
    private JButton firstPassButton = new JButton("Первый проход");
    private JButton secondPassButton = new JButton("Второй проход");


    private final Core core;

    public MainWindow() {
        core = new Core();

        setTitle("Двухпросмотровый ассемблер для программ в перемещаемом формате");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 750);
        setLayout(null);
        setResizable(false);

        JPanel group1 = new JPanel(null);
        group1.setBounds(10, 10, 330, 610);

        JLabel label1 = new JLabel("Исходный текст");
        label1.setBounds(110, 10, 200, 20);
        group1.add(label1);

        sourceCodeTextBox = new JTextArea();
        sourceCodeTextBox.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollSource = new JScrollPane(sourceCodeTextBox);
        scrollSource.setBounds(5, 35, 315, 340);
        group1.add(scrollSource);

        JLabel label2 = new JLabel("Таблица кодов операций");
        label2.setBounds(70, 380, 200, 20);
        group1.add(label2);

        String[] opCols = {"МКО", "Дв.Код", "Длина"};
        DefaultTableModel opModel = new DefaultTableModel(opCols, 0);
        operationCodeTable = new JTable(opModel);
        JScrollPane scrollOp = new JScrollPane(operationCodeTable);
        scrollOp.setBounds(60, 410, 210, 180);
        group1.add(scrollOp);

        add(group1);

        JPanel group2 = new JPanel(null);
        group2.setBounds(350, 10, 330, 610);

        JLabel label3 = new JLabel("Вспомогательная панель");
        label3.setBounds(80, 10, 200, 20);
        group2.add(label3);

        supportTable = new JTable(new DefaultTableModel(new String[]{"Адрес", "Команда", "Операнд1", "Операнд2"}, 0));
        JScrollPane scrollSupport = new JScrollPane(supportTable);
        scrollSupport.setBounds(5, 35, 315, 200);
        group2.add(scrollSupport);

        JLabel labelSupport = new JLabel("Вспомогательная таблица");
        labelSupport.setBounds(80, 240, 200, 20);
        group2.add(labelSupport);

        symbolTable = new JTable(new DefaultTableModel(new String[]{"Имя", "Адрес"}, 0));
        JScrollPane scrollSym = new JScrollPane(symbolTable);
        scrollSym.setBounds(5, 270, 150, 150);
        group2.add(scrollSym);

        JLabel labelSym = new JLabel("Символические имена");
        labelSym.setBounds(5, 430, 150, 20);
        group2.add(labelSym);

        settingTable = new JTable(new DefaultTableModel(new String[]{"Адрес"}, 0));
        JScrollPane scrollSetting = new JScrollPane(settingTable);
        scrollSetting.setBounds(170, 270, 150, 150);
        group2.add(scrollSetting);

        JLabel labelSetting = new JLabel("Таблица настроек");
        labelSetting.setBounds(170, 430, 150, 20);
        group2.add(labelSetting);

        firstPassErrorTextBox = new JTextArea();
        firstPassErrorTextBox.setEditable(false);
        JScrollPane scrollErr1 = new JScrollPane(firstPassErrorTextBox);
        scrollErr1.setBounds(5, 460, 315, 90);
        group2.add(scrollErr1);

        JLabel labelErr1 = new JLabel("Ошибки первого прохода");
        labelErr1.setBounds(80, 555, 200, 20);
        group2.add(labelErr1);

        add(group2);


        JPanel group3 = new JPanel(null);
        group3.setBounds(690, 10, 330, 610);

        JLabel label8 = new JLabel("Двоичный код");
        label8.setBounds(120, 10, 150, 20);
        group3.add(label8);

        binaryCodeTextBox = new JTextArea();
        binaryCodeTextBox.setEditable(false);
        binaryCodeTextBox.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollBin = new JScrollPane(binaryCodeTextBox);
        scrollBin.setBounds(5, 35, 315, 440);
        group3.add(scrollBin);

        JLabel label6 = new JLabel("Ошибки второго прохода");
        label6.setBounds(80, 485, 200, 20);
        group3.add(label6);

        secondPassErrorTextBox = new JTextArea();
        secondPassErrorTextBox.setEditable(false);
        JScrollPane scrollErr2 = new JScrollPane(secondPassErrorTextBox);
        scrollErr2.setBounds(5, 510, 315, 90);
        group3.add(scrollErr2);

        add(group3);

        firstPassButton.setBounds(70, 640, 200, 40);
        add(firstPassButton);

        secondPassButton.setBounds(410, 640, 200, 40);
        add(secondPassButton);

        firstPassButton.addActionListener(e -> performFirstPass());
        secondPassButton.addActionListener(e -> performSecondPass());

        secondPassButton.setEnabled(false);

        loadDefaultOperationCodes();
        loadExampleProgram();

        setVisible(true);

        JLabel labelAddressing = new JLabel("Тип адресации:");
        labelAddressing.setBounds(700, 640, 120, 20);
        add(labelAddressing);

        addressingModeComboBox = new JComboBox<>(new String[]{"Прямая", "Относительная", "Смешанная"});
        addressingModeComboBox.setBounds(800, 640, 200, 25);
        addressingModeComboBox.addActionListener(e -> {
            typeAdr = addressingModeComboBox.getSelectedIndex();
        });
        add(addressingModeComboBox);
    }

    private void performFirstPass() {
        ((DefaultTableModel) supportTable.getModel()).setRowCount(0);
        ((DefaultTableModel) symbolTable.getModel()).setRowCount(0);
        ((DefaultTableModel) settingTable.getModel()).setRowCount(0);
        firstPassErrorTextBox.setText("");

        String[][] sourceCode = parseSourceCode(sourceCodeTextBox.getText());
        if (sourceCode == null || sourceCode.length == 0) {
            JOptionPane.showMessageDialog(this, "Исходный код пуст.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < sourceCode.length; i++) {
            String[] row = sourceCode[i];
            String command = row.length > 1 ? row[1] : "";
            String operand1 = row.length > 2 ? row[2] : "";

            if (command == null || command.isEmpty()) continue;
            if (operand1 == null) operand1 = "";

            DefaultTableModel opModel = (DefaultTableModel) operationCodeTable.getModel();
            boolean isCommand = false;
            for (int j = 0; j < opModel.getRowCount(); j++) {
                Object val = opModel.getValueAt(j, 0);
                if (val != null && val.toString().equalsIgnoreCase(command)) {
                    isCommand = true;
                    break;
                }
            }
            if (!isCommand) continue;

            if (operand1.isEmpty()) continue;

            if (typeAdr == 0 && (operand1.contains("[") || operand1.contains("]"))) {
                firstPassErrorTextBox.setText("Ошибка. Относительная адресация в " + (i + 1) + " строке. В программе выбрана прямая адресация.");
                return;
            }

            if (typeAdr == 1
                    && (!operand1.contains("[") || !operand1.contains("]"))
                    && !isNumeric(operand1)
                    && !isRegister(operand1)) {
                firstPassErrorTextBox.setText("Ошибка. Прямая адресация в " + (i + 1) + " строке. В программе выбрана относительная адресация.");
                return;
            }
        }

        DefaultTableModel opModel = (DefaultTableModel) operationCodeTable.getModel();
        String[][] operationCode = new String[opModel.getRowCount()][3];
        for (int i = 0; i < opModel.getRowCount(); i++) {
            for (int j = 0; j < 3; j++) {
                Object val = opModel.getValueAt(i, j);
                operationCode[i][j] = (val == null) ? "" : val.toString();
            }
        }

        boolean success = core.doFirstPass(sourceCode, operationCode, supportTable, symbolTable);

        if (success) {
            firstPassErrorTextBox.setText("Первый проход успешно завершён ✅");
            secondPassButton.setEnabled(true);
        } else {
            firstPassErrorTextBox.setText(core.errorText);
            secondPassButton.setEnabled(false);
        }
    }


    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isRegister(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("(?i)R\\d{1,2}");
    }

    private String[][] parseSourceCode(String sourceText) {
        core.clearTables(supportTable, symbolTable);

        String[] lines = sourceText.split("\n");
        String[][] sourceCode = new String[lines.length][4];

        System.out.println("=== Начало разбора исходного кода ===");

        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String line = raw.trim();

            if (line.isEmpty()) {
                Arrays.fill(sourceCode[i], "");
                System.out.println((i + 1) + ": [ПУСТАЯ СТРОКА]");
                continue;
            }

            boolean hasLeadingSpace = raw.length() > 0 && Character.isWhitespace(raw.charAt(0));
            String[] parts = new String[4];
            Arrays.fill(parts, "");

            int idx = 0;  // индекс для записи в parts
            int pos = 0;  // текущая позиция в строке

            while (pos < line.length() && idx < 4) {
                // Пропускаем пробелы
                while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) pos++;
                if (pos >= line.length()) break;

                // Если встретили C" или X"
                if (pos + 1 < line.length() && (line.charAt(pos) == 'C' || line.charAt(pos) == 'X') && line.charAt(pos + 1) == '"') {
                    int firstQuote = pos + 1;
                    int lastQuote = line.lastIndexOf('"');
                    if (lastQuote == -1 || lastQuote <= firstQuote) {
                        // Закрывающей кавычки нет — читаем до конца
                        parts[idx++] = line.substring(pos).toUpperCase();
                        pos = line.length();
                    } else {
                        // Берём всю строку от первой до последней кавычки
                        parts[idx++] = line.substring(pos, lastQuote + 1).toUpperCase();
                        pos = lastQuote + 1;
                    }
                } else {
                    // обычный токен до следующего пробела
                    int start = pos;
                    while (pos < line.length() && !Character.isWhitespace(line.charAt(pos))) pos++;
                    parts[idx++] = line.substring(start, pos).toUpperCase();
                }
            }

            // если первая колонка — пробел в начале строки
            if (hasLeadingSpace) {
                sourceCode[i][0] = "";
                for (int j = 0; j < 3; j++) {
                    sourceCode[i][j + 1] = parts[j] != null ? parts[j] : "";
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    sourceCode[i][j] = parts[j] != null ? parts[j] : "";
                }
            }

            System.out.printf(
                    "%2d | %-50s | mark='%s', OC='%s', OP1='%s', OP2='%s'%n",
                    (i + 1),
                    raw,
                    sourceCode[i][0],
                    sourceCode[i][1],
                    sourceCode[i][2],
                    sourceCode[i][3]
            );
        }

        System.out.println("=== Конец разбора ===\n");
        return sourceCode;
    }

    private void performSecondPass() {
        secondPassErrorTextBox.setText("");
        binaryCodeTextBox.setText("");

        boolean success = core.doSecondPass(binaryCodeTextBox, settingTable);

        if (success && core.errorText.isEmpty()) {
            secondPassErrorTextBox.setText("Второй проход успешно завершён ✅");
        } else {
            secondPassErrorTextBox.setText(core.errorText);
        }
    }

    private void loadDefaultOperationCodes() {
        DefaultTableModel model = (DefaultTableModel) operationCodeTable.getModel();

        model.setRowCount(0);

        String[][] defaultOperations = {
                {"JMP", "01", "4"},
                {"LOADR1", "02", "4"},
                {"LOADR2", "03", "4"},
                {"ADD", "04", "2"},
                {"SAVER1", "05", "4"},
                {"NOP", "06", "1"},
                {"INT", "07", "2"},
                {"SUB", "08", "2"},
        };

        for (String[] operation : defaultOperations) {
            model.addRow(operation);
        }
    }

    private void loadExampleProgram() {
        String exampleProgram =
                "PROG START 0\n" +
                        "     JMP     L1\n" +
                        "A1   RESB    10\n" +
                        "A2   RESW    20\n" +
                        "B1   WORD    4096\n" +
                        "B2   BYTE    X\"2F4C008A\"\n" +
                        "B3   BYTE    C\"Hello,Assembler!\"\n" +
                        "B4   BYTE    128\n" +
                        "L1   LOADR1  B1\n" +
                        "     LOADR2  B4\n" +
                        "     ADD     R1 R2\n" +
                        "     SUB     R1 R2\n" +
                        "     SAVER1  B1\n" +
                        "     NOP\n" +
                        "     END  ";

        sourceCodeTextBox.setText(exampleProgram);
    }
}
