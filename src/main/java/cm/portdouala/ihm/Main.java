package cm.portdouala.ihm;

import fr.tp308.ihm.service.IConteneurService;
import fr.tp308.ihm.service.MockConteneurService;
import fr.tp308.ihm.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class Main {

    private Main() {
        // Utility class
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            IConteneurService service = new MockConteneurService();
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Impossible de charger le look and feel : " + e.getMessage());
        }
    }
}
