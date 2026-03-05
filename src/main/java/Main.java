import view.AdminDashboard;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 12);
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf Dark theme.");
        }
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }
}