package lab1;

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
    private JButton firstPassButton = new JButton("Первый проход");
    private JButton secondPassButton = new JButton("Второй проход");


    private final Core core;

    public MainWindow() {
        core = new Core();

        setTitle("Двухпросмотровый ассемблер для программ в абсолютном формате");
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

        JLabel label3 = new JLabel("Вспомогательная таблица");
        label3.setBounds(80, 10, 200, 20);
        group2.add(label3);

        supportTable = new JTable(new DefaultTableModel(new String[]{"Адрес", "Команда", "Операнд1", "Операнд2"}, 0));
        JScrollPane scrollSupport = new JScrollPane(supportTable);
        scrollSupport.setBounds(5, 35, 315, 270);
        group2.add(scrollSupport);

        JLabel label4 = new JLabel("Таблица символических имен");
        label4.setBounds(70, 315, 220, 20);
        group2.add(label4);

        String[] symCols = {"Имя", "Адрес"};
        symbolTable = new JTable(new DefaultTableModel(symCols, 0));
        JScrollPane scrollSym = new JScrollPane(symbolTable);
        scrollSym.setBounds(60, 340, 210, 140);
        group2.add(scrollSym);

        JLabel label5 = new JLabel("Ошибки первого прохода");
        label5.setBounds(80, 485, 200, 20);
        group2.add(label5);

        firstPassErrorTextBox = new JTextArea();
        firstPassErrorTextBox.setEditable(false);
        JScrollPane scrollErr1 = new JScrollPane(firstPassErrorTextBox);
        scrollErr1.setBounds(5, 510, 315, 90);
        group2.add(scrollErr1);

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
    }

    private void performFirstPass() {
        // Очистка старых данных
        ((DefaultTableModel) supportTable.getModel()).setRowCount(0);
        ((DefaultTableModel) symbolTable.getModel()).setRowCount(0);
        firstPassErrorTextBox.setText("");

        // Получаем исходный код
        String[] lines = sourceCodeTextBox.getText().split("\n");
        String[][] sourceCode = parseSourceCode(sourceCodeTextBox.getText());

        // Получаем таблицу кодов операций
        DefaultTableModel opModel = (DefaultTableModel) operationCodeTable.getModel();
        String[][] operationCode = new String[opModel.getRowCount()][3];
        for (int i = 0; i < opModel.getRowCount(); i++) {
            for (int j = 0; j < 3; j++) {
                operationCode[i][j] = (String) opModel.getValueAt(i, j);
            }
        }

        // Вызываем первый проход
        boolean success = core.doFirstPass(sourceCode, operationCode, supportTable, symbolTable);

        // Выводим результат
        if (success) {
            firstPassErrorTextBox.setText("Первый проход успешно завершён ✅");
            secondPassButton.setEnabled(true);
        } else {
            firstPassErrorTextBox.setText(core.errorText);
            secondPassButton.setEnabled(false);
        }

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
            String[] parts = line.split("\\s+", 4);

            if (hasLeadingSpace) {
                sourceCode[i][0] = "";
                for (int j = 0; j < parts.length; j++) {
                    if (parts[j].startsWith("C\"") && parts[j].endsWith("\"")) {
                        sourceCode[i][j + 1] = parts[j];
                    } else {
                        sourceCode[i][j + 1] = parts[j].toUpperCase();
                    }
                }
                for (int j = parts.length + 1; j < 4; j++) sourceCode[i][j] = "";
            } else {
                for (int j = 0; j < 4; j++) {
                    if (j < parts.length) {
                        if (parts[j].startsWith("C\"") && parts[j].endsWith("\"")) {
                            sourceCode[i][j] = parts[j]; // сохраняем регистр
                        } else {
                            sourceCode[i][j] = parts[j].toUpperCase();
                        }
                    } else sourceCode[i][j] = "";
                }
            }
            System.out.printf(
                    "%2d | %-20s | mark='%s', OC='%s', OP1='%s', OP2='%s'%n",
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

        boolean success = core.doSecondPass(binaryCodeTextBox);

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
                "PROG START 100\n" +
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
                        "     END     100";

        sourceCodeTextBox.setText(exampleProgram);
    }
}
