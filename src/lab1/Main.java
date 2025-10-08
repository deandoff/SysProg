package lab1;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Устанавливаем нативный Look and Feel для лучшей интеграции с ОС
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException |
                 InstantiationException | IllegalAccessException e) {
            // В случае ошибки используем стандартный Look and Feel
            System.err.println("Ошибка установки Look and Feel: " + e.getMessage());
        }

        // Запускаем приложение в EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        MainWindow mainWindow = new MainWindow();
        mainWindow.setLocationRelativeTo(null); // Центрирование окна
        mainWindow.setVisible(true);
    }
}