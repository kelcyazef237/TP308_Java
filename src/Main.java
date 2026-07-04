import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import metier.ui.FenetrePrincipale;
import metier.ui.icons.SvgLoader;
import metier.ui.theme.GestionnaireTheme;

import javax.swing.*;

/**
 * Point d'entree de l'application de gestion du fret portuaire.
 * Port Autonome de Douala.
 *
 * @author Equipe Core & Metier
 * @version 2.0
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialiser le theme (lit prefs.properties, defaut sombre)
        GestionnaireTheme.initialiser();

        // 2. Choisir FlatLaf en fonction du theme
        try {
            if (GestionnaireTheme.estSombre()) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            // Re-appliquer les cles UIManager specifiques (apres FlatLaf.setup)
            GestionnaireTheme.appliquerUIManager(GestionnaireTheme.actif());

            // Forcer la police par defaut (systeme) pour eviter Segoe UI manquant
            UIManager.put("defaultFont", new JLabel().getFont());

        } catch (Exception e) {
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // 3. Precharger les icones Lucide
        SvgLoader.precharger(SvgLoader.iconesParDefaut());

        // 4. Lancer l'interface sur le thread EDT
        SwingUtilities.invokeLater(() -> {
            try {
                FenetrePrincipale fenetre = new FenetrePrincipale();
                fenetre.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Erreur au lancement de l'application :\n" + e.getMessage(),
                    "Erreur Fatale", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
