package lab4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.swing.UIManager.setLookAndFeel;

public class MainWindow extends JFrame {
    // Компоненты группы 1
    private JPanel groupBox1;
    private JTextArea sourceCodeTextBox;
    private JTable operationCodeTable;
    private JLabel label1;
    private JLabel label2;

    // Компоненты группы 2
    private JPanel groupBox2;
    private JTextArea firstPassErrorTextBox;
    private JTable symbolTable;
    private JLabel label3;
    private JLabel label5;

    // Компоненты группы 3
    private JPanel groupBox3;
    private JTable binaryCodeTable;
    private JLabel label8;

    // Кнопки управления
    private JButton startButton;
    private JButton stepButton;
    private JButton restartButton;
    private JComboBox<String> exampleComboBox;
    private JLabel label9;
    private JButton addOpButton;
    private JButton deleteOpButton;

    // Переменные состояния
    private boolean prepareFlag = false;
    private int typeAdr = 0;
    private int currentRow = 0;
    private Core core;
    private DataCheck dC;

    // Данные
    private String[][] operationCodeArray;
    private String[][] sourceCodeArray;

    public MainWindow() {
        setLookAndFeel();
        core = new Core();
        dC = new DataCheck();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDefaultData();
    }

    private void initializeComponents() {
        // Группа 1 - Исходный код и таблица операций
        groupBox1 = new JPanel();
        groupBox1.setLayout(null);
        groupBox1.setBorder(BorderFactory.createTitledBorder(""));

        label1 = new JLabel("Исходный текст");
        label1.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        sourceCodeTextBox = new JTextArea();
        sourceCodeTextBox.setFont(new Font("Consolas", Font.PLAIN, 14));
        sourceCodeTextBox.setBackground(Color.WHITE);

        label2 = new JLabel("Таблица кодов операций");
        label2.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        // Таблица кодов операций
        String[] opColumns = {"МКО", "Дв.Код", "Длина"};
        DefaultTableModel opModel = new DefaultTableModel(opColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        operationCodeTable = new JTable(opModel);
        operationCodeTable.setRowHeight(25);

        // Группа 2 - Таблица символов и ошибки
        groupBox2 = new JPanel();
        groupBox2.setLayout(null);
        groupBox2.setBorder(BorderFactory.createTitledBorder(""));

        label3 = new JLabel("Таблица символьных имён");
        label3.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        // Таблица символов
        String[] symbolColumns = {"Симв. Имя", "Адрес Симв. Имени", "Значение счет. Адреса"};
        DefaultTableModel symbolModel = new DefaultTableModel(symbolColumns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setRowHeight(25);

        label5 = new JLabel("Ошибки");
        label5.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        firstPassErrorTextBox = new JTextArea();
        firstPassErrorTextBox.setEditable(false);
        firstPassErrorTextBox.setBackground(Color.WHITE);

        // Группа 3 - Объектный модуль
        groupBox3 = new JPanel();
        groupBox3.setLayout(null);
        groupBox3.setBorder(BorderFactory.createTitledBorder(""));

        label8 = new JLabel("Объектный модуль");
        label8.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        // Таблица бинарного кода
        String[] binaryColumns = {"", "", "", ""};
        DefaultTableModel binaryModel = new DefaultTableModel(binaryColumns, 0);
        binaryCodeTable = new JTable(binaryModel);
        binaryCodeTable.setRowHeight(25);
        binaryCodeTable.setTableHeader(null);

        // Кнопки управления
        startButton = new JButton("Запуск/Продолжить");
        startButton.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        stepButton = new JButton("Один шаг");
        stepButton.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        restartButton = new JButton("Перезапуск");
        restartButton.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        exampleComboBox = new JComboBox<>(new String[]{"Прямая адресация"});

        label9 = new JLabel("Выбор примера");
        label9.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        addOpButton = new JButton("Добавить");
        addOpButton.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));

        deleteOpButton = new JButton("Удалить");
        deleteOpButton.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 10));
    }

    private void setupLayout() {
        setTitle("Однопросмотровый ассемблер для программы в абсолютном формате");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 760);
        setLayout(null);
        setResizable(false);

        // Группа 1
        groupBox1.setBounds(12, 12, 329, 643);
        label1.setBounds(107, 12, 113, 17);
        sourceCodeTextBox.setBounds(6, 32, 317, 345);
        label2.setBounds(73, 389, 175, 17);
        operationCodeTable.setBounds(60, 409, 202, 188);

        JScrollPane sourceScroll = new JScrollPane(sourceCodeTextBox);
        sourceScroll.setBounds(6, 32, 317, 345);
        groupBox1.add(sourceScroll);

        JScrollPane opScroll = new JScrollPane(operationCodeTable);
        opScroll.setBounds(60, 409, 202, 188);
        groupBox1.add(opScroll);

        groupBox1.add(label1);
        groupBox1.add(label2);
        add(groupBox1);

        // Группа 2
        groupBox2.setBounds(347, 12, 444, 643);
        label3.setBounds(140, 12, 184, 17);
        label5.setBounds(194, 487, 61, 17);
        firstPassErrorTextBox.setBounds(6, 507, 430, 90);

        JScrollPane symbolScroll = new JScrollPane(symbolTable);
        symbolScroll.setBounds(6, 32, 430, 439);
        groupBox2.add(symbolScroll);

        JScrollPane errorScroll = new JScrollPane(firstPassErrorTextBox);
        errorScroll.setBounds(6, 507, 430, 90);
        groupBox2.add(errorScroll);

        groupBox2.add(label3);
        groupBox2.add(label5);
        add(groupBox2);

        // Группа 3
        groupBox3.setBounds(789, 12, 342, 643);
        label8.setBounds(117, 12, 135, 17);

        JScrollPane binaryScroll = new JScrollPane(binaryCodeTable);
        binaryScroll.setBounds(6, 32, 330, 565);
        groupBox3.add(binaryScroll);

        groupBox3.add(label8);
        add(groupBox3);

        // Кнопки управления
        startButton.setBounds(72, 667, 202, 45);
        stepButton.setBounds(353, 667, 202, 45);
        restartButton.setBounds(581, 667, 202, 45);
        exampleComboBox.setBounds(795, 691, 330, 21);
        label9.setBounds(916, 672, 112, 17);

        addOpButton.setBounds(60, 600, 80, 30);
        deleteOpButton.setBounds(150, 600, 80, 30);

        groupBox1.add(addOpButton);
        groupBox1.add(deleteOpButton);

        add(startButton);
        add(stepButton);
        add(restartButton);
        add(exampleComboBox);
        add(label9);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> begin(2));
        stepButton.addActionListener(e -> begin(0));
        restartButton.addActionListener(e -> begin(1));
        addOpButton.addActionListener(e -> addOperationCode());
        deleteOpButton.addActionListener(e -> deleteOperationCode());

        exampleComboBox.addActionListener(e -> {
            exampleComboBoxSelectedIndexChanged();
        });

        sourceCodeTextBox.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { sourceCodeTextBoxTextChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { sourceCodeTextBoxTextChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { sourceCodeTextBoxTextChanged(); }
        });
    }

    // Основные методы из C# кода

    private void begin(int param) {
        if (!prepareFlag) {
            firstPassErrorTextBox.setText("");
            if (prepare()) {
                prepareFlag = true;
                protectSourceData();
                firstPassErrorTextBox.setText("");
            } else {
                stop();
                return;
            }
        }

        switch (param) {
            case 0: // Один шаг
                if (currentRow + 1 <= sourceCodeArray.length) {
                    if (!core.doPass(sourceCodeArray, operationCodeArray, symbolTable, binaryCodeTable, currentRow)) {
                        writeError(firstPassErrorTextBox, core.getErrorText());
                        stop();
                        return;
                    }

                    if (core.isFlagEnd()) {
                        stop();
                        return;
                    }
                    currentRow++;
                } else {
                    if (!core.isFlagEnd()) {
                        writeError(firstPassErrorTextBox, "Ошибка: Не найдена директива END");
                        stop();
                        return;
                    }
                }
                break;

            case 1: // Перезапуск
                stop();
                break;

            case 2: // Запуск/Продолжить
                for (int i = currentRow; i <= sourceCodeArray.length; i++) {
                    if (i + 1 <= sourceCodeArray.length) {
                        if (!core.doPass(sourceCodeArray, operationCodeArray, symbolTable, binaryCodeTable, i)) {
                            writeError(firstPassErrorTextBox, core.getErrorText());
                            stop();
                            return;
                        }

                        if (core.isFlagEnd()) {
                            stop();
                            return;
                        }
                    } else {
                        if (!core.isFlagEnd()) {
                            writeError(firstPassErrorTextBox, "Ошибка: не найдена директива END");
                            stop();
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void stop() {
        unprotectSourceData();
        core = new Core(); // Пересоздаем ядро
        prepareFlag = false;
        currentRow = 0;
    }

    private boolean prepare() {
        currentRow = 0;
        clear();
        deleteEmptyRows();

        if (parser()) {
            if (core.checkOperationCode(operationCodeArray)) {
                return true;
            } else {
                writeError(firstPassErrorTextBox, core.getErrorText());
                return false;
            }
        } else {
            return false;
        }
    }

    private void protectSourceData() {
        operationCodeTable.setEnabled(false);
        sourceCodeTextBox.setEditable(false);
    }

    private void unprotectSourceData() {
        operationCodeTable.setEnabled(true);
        sourceCodeTextBox.setEditable(true);
    }

    private void writeError(JTextArea textArea, String str) {
        textArea.setText(textArea.getText() + str + "\n");
    }

    private void clear() {
        ((DefaultTableModel) symbolTable.getModel()).setRowCount(0);
        firstPassErrorTextBox.setText("");
        ((DefaultTableModel) binaryCodeTable.getModel()).setRowCount(0);
    }

    private void sourceCodeTextBoxTextChanged() {
        clear();
    }

    private void deleteEmptyRows() {
        DefaultTableModel model = (DefaultTableModel) operationCodeTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            boolean empty = true;
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                if (value != null && !value.toString().isEmpty()) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                model.removeRow(i);
            }
        }
    }

    private boolean parser() {
        clear();
        deleteEmptyRows();

        List<String> marks = new ArrayList<>();

        DefaultTableModel opModel = (DefaultTableModel) operationCodeTable.getModel();
        operationCodeArray = new String[opModel.getRowCount()][opModel.getColumnCount()];

        for (int i = 0; i < opModel.getRowCount(); i++) {
            for (int j = 0; j < opModel.getColumnCount(); j++) {
                Object value = opModel.getValueAt(i, j);
                operationCodeArray[i][j] = (value != null ? value.toString() : "").toUpperCase();
            }
        }

        String[] str = sourceCodeTextBox.getText().split("\n");
        str = Arrays.stream(str)
                .map(s -> s.replace("\r", ""))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        sourceCodeArray = new String[str.length][4]; // 4 колонки

        for (int i = 0; i < str.length; i++) {
            Arrays.fill(sourceCodeArray[i], "");
        }

        for (int i = 0; i < str.length; i++) {
            str[i] = str[i].trim();
            String[] temp = str[i].split(" ");

            // Обработка строк с кавычками (как в C# коде)
            if (temp.length >= 3) {
                if (temp[2].indexOf('"') == 1 && temp[temp.length - 1].lastIndexOf('"') == temp[temp.length - 1].length() - 1) {
                    for (int j = 3; j < temp.length; j++) {
                        temp[2] += " " + temp[j];
                        temp[j] = "";
                    }
                } else if (temp[1].indexOf('"') == 1 && temp[temp.length - 1].lastIndexOf('"') == temp[temp.length - 1].length() - 1) {
                    for (int j = 2; j < temp.length; j++) {
                        temp[1] += " " + temp[j];
                        temp[j] = "";
                    }
                }
            }

            temp = Arrays.stream(temp)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            if (temp.length > 0 && "END".equals(temp[0]) && i + 1 != str.length) {
                JOptionPane.showMessageDialog(this, "Замечены строки после END.", "Внимание!", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if (temp.length <= 4) {
                if (temp.length == 1) {
                    if (dC.checkDirective(temp[0]) || core.findCode(temp[0], operationCodeArray) != -1) {
                        sourceCodeArray[i][1] = temp[0];
                    } else {
                        for (int j = 0; j < temp.length; j++) {
                            sourceCodeArray[i][j] = temp[j];
                        }
                    }
                } else if (temp.length == 2) {
                    if ("EXTREF".equals(temp[0]) || "EXTDEF".equals(temp[0])) {
                        marks.add(temp[1]);
                    }

                    if (dC.checkDirective(temp[0]) || core.findCode(temp[0], operationCodeArray) != -1) {
                        // Проверка типа адресации
                        if (core.findCode(temp[0], operationCodeArray) != -1 && typeAdr == 0 &&
                                (temp[1].contains("[") || temp[1].contains("]")) && !marks.contains(temp[1])) {
                            JOptionPane.showMessageDialog(this,
                                    "Относительная адресация в " + (i + 1) + " строке. Выбрана прямая адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        if (core.findCode(temp[0], operationCodeArray) != -1 && typeAdr == 1 &&
                                (!temp[1].contains("[") || !temp[1].contains("]")) && !marks.contains(temp[1]) &&
                                !isNumeric(temp[1])) {
                            JOptionPane.showMessageDialog(this,
                                    "Прямая адресация в " + (i + 1) + " строке. Выбрана относительная адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        sourceCodeArray[i][1] = temp[0];
                        sourceCodeArray[i][2] = temp[1];
                    } else if (dC.checkDirective(temp[1]) || core.findCode(temp[1], operationCodeArray) != -1) {
                        sourceCodeArray[i][0] = temp[0];
                        sourceCodeArray[i][1] = temp[1];
                    } else {
                        for (int j = 0; j < temp.length; j++) {
                            sourceCodeArray[i][j] = temp[j];
                        }
                    }
                } else if (temp.length == 3) {
                    if (dC.checkDirective(temp[0]) || core.findCode(temp[0], operationCodeArray) != -1) {
                        sourceCodeArray[i][1] = temp[0];
                        sourceCodeArray[i][2] = temp[1];
                        sourceCodeArray[i][3] = temp[2];
                    } else if (dC.checkDirective(temp[1]) || core.findCode(temp[1], operationCodeArray) != -1) {
                        // Проверка типа адресации
                        if (core.findCode(temp[1], operationCodeArray) != -1 && typeAdr == 0 &&
                                (temp[2].contains("[") || temp[2].contains("]")) && !marks.contains(temp[2])) {
                            JOptionPane.showMessageDialog(this,
                                    "Относительная адресация в " + (i + 1) + " строке. Выбрана прямая адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        if (core.findCode(temp[1], operationCodeArray) != -1 && typeAdr == 1 &&
                                (!temp[2].contains("[") || !temp[2].contains("]")) && !marks.contains(temp[2]) &&
                                !isNumeric(temp[2])) {
                            JOptionPane.showMessageDialog(this,
                                    "Прямая адресация в " + (i + 1) + " строке. Выбрана относительная адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        sourceCodeArray[i][0] = temp[0];
                        sourceCodeArray[i][1] = temp[1];
                        sourceCodeArray[i][2] = temp[2];
                    } else {
                        for (int j = 0; j < temp.length; j++) {
                            sourceCodeArray[i][j] = temp[j];
                        }
                    }
                } else if (temp.length == 4) {
                    if (dC.checkDirective(temp[1]) || core.findCode(temp[1], operationCodeArray) != -1) {
                        // Проверка типа адресации
                        if (core.findCode(temp[1], operationCodeArray) != -1 && typeAdr == 0 &&
                                (temp[2].contains("[") || temp[2].contains("]")) && !marks.contains(temp[2])) {
                            JOptionPane.showMessageDialog(this,
                                    "Относительная адресация в " + (i + 1) + " строке. Выбрана прямая адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        if (core.findCode(temp[1], operationCodeArray) != -1 && typeAdr == 1 &&
                                (!temp[2].contains("[") || !temp[2].contains("]")) && !marks.contains(temp[2]) &&
                                !isNumeric(temp[2])) {
                            JOptionPane.showMessageDialog(this,
                                    "Прямая адресация в " + (i + 1) + " строке. Выбрана относительная адресация.",
                                    "Внимание!", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                        sourceCodeArray[i][0] = temp[0];
                        sourceCodeArray[i][1] = temp[1];
                        sourceCodeArray[i][2] = temp[2];
                        sourceCodeArray[i][3] = temp[3];
                    } else if (!(dC.checkDirective(temp[1]) || core.findCode(temp[1], operationCodeArray) != -1)) {
                        JOptionPane.showMessageDialog(this,
                                "Синтаксическая ошибка в " + (i + 1) + " строке.",
                                "Внимание!", JOptionPane.WARNING_MESSAGE);
                        return false;
                    } else {
                        for (int j = 0; j < temp.length; j++) {
                            sourceCodeArray[i][j] = temp[j];
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Синтаксическая ошибка в " + (i + 1) + " строке. Элементов в строке больше 4",
                        "Внимание!", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        // Приведение к верхнему регистру
        for (int i = 0; i < sourceCodeArray.length; i++) {
            if (sourceCodeArray[i][1] != null) {
                sourceCodeArray[i][1] = sourceCodeArray[i][1].toUpperCase();
            }
        }

        return true;
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

    private void exampleComboBoxSelectedIndexChanged() {
        clear();
        if (exampleComboBox.getSelectedIndex() == 0) {
            sourceCodeTextBox.setText(
                    "prog start 100\n" +
                            "jmp L1\n" +
                            "A1 RESB 10\n" +
                            "A2 RESW 20\n" +
                            "B1 WORD 4096\n" +
                            "LOADR1 L1\n" +
                            "B2 BYTE X\"2F4C008A\"\n" +
                            "B3 BYTE C\"Hello!\"\n" +
                            "B4 BYTE 128\n" +
                            "L1 LOADR1 B1\n" +
                            "LOADR2 B4\n" +
                            "ADD R1 R2\n" +
                            "SAVER1 B1\n" +
                            "NOP\n" +
                            "END 100"
            );
            typeAdr = 0;
        }
    }

    private void loadDefaultData() {
        loadDefaultOperationCodes();
        exampleComboBox.setSelectedIndex(0);
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
        };

        for (String[] operation : defaultOperations) {
            model.addRow(operation);
        }
    }

    private void addOperationCode() {
        DefaultTableModel model = (DefaultTableModel) operationCodeTable.getModel();
        model.addRow(new Object[]{"", "", ""});
    }

    private void deleteOperationCode() {
        DefaultTableModel model = (DefaultTableModel) operationCodeTable.getModel();
        int selectedRow = operationCodeTable.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Выберите команду для удаления",
                    "Внимание",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            System.err.println("Не удалось установить Look and Feel: " + e.getMessage());
        }
    }
}