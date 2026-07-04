package metier.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Gestionnaire global du theme (sombre/clair).
 * Singleton mutable : {@link #initialiser()} doit etre appele au demarrage
 * de l'app, puis {@link #definirMode(boolean)} permet de basculer a chaud.
 */
public final class GestionnaireTheme {

    public static final String PROPRIETE_THEME = "theme";
    private static final String FICHIER_PREFS = "prefs.properties";
    private static final String CLE_THEME = "theme";

    private static Theme themeActif = Theme.sombre();
    private static final PropertyChangeSupport PCS = new PropertyChangeSupport(GestionnaireTheme.class);
    private static boolean initialise = false;

    private GestionnaireTheme() {}

    /** Initialise le theme depuis prefs.properties (defaut : sombre). */
    public static synchronized void initialiser() {
        if (initialise) return;
        Properties props = lirePrefs();
        String mode = props.getProperty(CLE_THEME, "sombre");
        themeActif = "clair".equalsIgnoreCase(mode) ? Theme.clair() : Theme.sombre();
        appliquerUIManager(themeActif);
        initialise = true;
    }

    /** Retourne le theme actuellement actif. */
    public static Theme actif() {
        return themeActif;
    }

    /** Bascule entre sombre et clair, sauvegarde dans prefs.properties. */
    public static synchronized void definirMode(boolean sombre) {
        Theme ancien = themeActif;
        Theme nouveau = sombre ? Theme.sombre() : Theme.clair();
        themeActif = nouveau;
        try {
            if (sombre) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        appliquerUIManager(nouveau);
        mettreAJourUI();
        sauvegarderPrefs(sombre);
        PCS.firePropertyChange(PROPRIETE_THEME, ancien, nouveau);
    }

    public static boolean estSombre() {
        return themeActif.estSombre();
    }

    public static void ajouterEcouteur(PropertyChangeListener listener) {
        PCS.addPropertyChangeListener(listener);
    }

    public static void retirerEcouteur(PropertyChangeListener listener) {
        PCS.removePropertyChangeListener(listener);
    }

    /** Applique les couleurs du theme a UIManager (pour FlatLaf et tous les composants L&F). */
    public static void appliquerUIManager(Theme t) {
        // Couleurs de base
        UIManager.put("Panel.background", t.bg());
        UIManager.put("Label.foreground", t.textePrimaire());
        UIManager.put("Label.disabledForeground", t.texteAtténué());
        UIManager.put("Button.background", t.surface());
        UIManager.put("Button.foreground", t.textePrimaire());
        UIManager.put("Button.disabledBackground", t.surface());
        UIManager.put("Button.disabledForeground", t.texteAtténué());
        UIManager.put("Button.hoverBackground", t.surfaceElevee());
        UIManager.put("Button.pressedBackground", t.bordure());
        UIManager.put("TextField.background", t.surface());
        UIManager.put("TextField.foreground", t.textePrimaire());
        UIManager.put("TextField.placeholderForeground", t.texteAtténué());
        UIManager.put("TextField.caretForeground", t.accent());
        UIManager.put("TextField.selectionBackground", t.selectionBg());
        UIManager.put("TextField.selectionForeground", t.selectionFg());
        UIManager.put("TextArea.background", t.surface());
        UIManager.put("TextArea.foreground", t.textePrimaire());
        UIManager.put("TextArea.caretForeground", t.accent());
        UIManager.put("PasswordField.background", t.surface());
        UIManager.put("PasswordField.foreground", t.textePrimaire());

        // Combo
        UIManager.put("ComboBox.background", t.surface());
        UIManager.put("ComboBox.foreground", t.textePrimaire());
        UIManager.put("ComboBox.selectionBackground", t.selectionBg());
        UIManager.put("ComboBox.selectionForeground", t.selectionFg());
        UIManager.put("ComboBox.buttonBackground", t.surface());
        UIManager.put("ComboBox.buttonHoverBackground", t.surfaceElevee());

        // Table
        UIManager.put("Table.background", t.surface());
        UIManager.put("Table.foreground", t.textePrimaire());
        UIManager.put("Table.selectionBackground", t.selectionBg());
        UIManager.put("Table.selectionForeground", t.selectionFg());
        UIManager.put("Table.gridColor", t.bordure());
        UIManager.put("TableHeader.background", t.surfaceElevee());
        UIManager.put("TableHeader.foreground", t.texteSecondaire());
        UIManager.put("TableHeader.bottomSeparatorColor", t.bordure());
        UIManager.put("TableHeader.hoverBackground", t.surfaceElevee());
        UIManager.put("TableHeader.pressedBackground", t.bordure());

        // List
        UIManager.put("List.background", t.surface());
        UIManager.put("List.foreground", t.textePrimaire());
        UIManager.put("List.selectionBackground", t.selectionBg());
        UIManager.put("List.selectionForeground", t.selectionFg());

        // ScrollBar
        UIManager.put("ScrollBar.thumb", t.bordureForte());
        UIManager.put("ScrollBar.hoverThumbColor", t.texteAtténué());
        UIManager.put("ScrollBar.track", t.bg());
        UIManager.put("ScrollBar.background", t.bg());

        // Menu
        UIManager.put("MenuBar.background", t.surface());
        UIManager.put("Menu.background", t.surface());
        UIManager.put("Menu.foreground", t.textePrimaire());
        UIManager.put("MenuItem.background", t.surface());
        UIManager.put("MenuItem.foreground", t.textePrimaire());
        UIManager.put("MenuItem.selectionBackground", t.selectionBg());
        UIManager.put("MenuItem.selectionForeground", t.selectionFg());
        UIManager.put("PopupMenu.background", t.surface());
        UIManager.put("PopupMenu.foreground", t.textePrimaire());
        UIManager.put("PopupMenu.borderColor", t.bordure());

        // TitledBorder
        UIManager.put("TitledBorder.titleColor", t.texteSecondaire());

        // OptionPane
        UIManager.put("OptionPane.background", t.surface());
        UIManager.put("OptionPane.foreground", t.textePrimaire());
        UIManager.put("OptionPane.messageForeground", t.textePrimaire());

        // Tooltip
        UIManager.put("ToolTip.background", t.surfaceElevee());
        UIManager.put("ToolTip.foreground", t.textePrimaire());
        UIManager.put("ToolTip.borderColor", t.bordure());

        // CheckBox / Radio
        UIManager.put("CheckBox.background", t.bg());
        UIManager.put("CheckBox.foreground", t.textePrimaire());
        UIManager.put("CheckBox.icon.background", t.surface());
        UIManager.put("CheckBox.icon.checkmarkColor", t.accent());
        UIManager.put("RadioButton.background", t.bg());
        UIManager.put("RadioButton.foreground", t.textePrimaire());

        // Component focus
        UIManager.put("Component.focusColor", t.focus());
        UIManager.put("Component.focusedBorderColor", t.focus());
        UIManager.put("Button.focusedBorderColor", t.focus());

        // ProgressBar
        UIManager.put("ProgressBar.background", t.surfaceElevee());
        UIManager.put("ProgressBar.foreground", t.accent());
        UIManager.put("ProgressBar.selectionBackground", t.bg());
        UIManager.put("ProgressBar.selectionForeground", t.accent());

        // Arcs FlatLaf
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollPane.arc", 10);
        UIManager.put("ProgressBar.arc", 6);
        UIManager.put("CheckBox.arc", 4);
    }

    /** Notifie toutes les fenetres ouvertes qu'elles doivent recharger leur UI. */
    private static void mettreAJourUI() {
        Window[] windows = Window.getWindows();
        for (Window w : windows) {
            SwingUtilities.updateComponentTreeUI(w);
            w.invalidate();
            w.validate();
            w.repaint();
        }
    }

    private static Properties lirePrefs() {
        Properties props = new Properties();
        try {
            Path p = Path.of(FICHIER_PREFS);
            if (Files.exists(p)) {
                try (var in = Files.newInputStream(p)) {
                    props.load(in);
                }
            }
        } catch (IOException ignored) {
        }
        return props;
    }

    private static void sauvegarderPrefs(boolean sombre) {
        Properties props = new Properties();
        props.setProperty(CLE_THEME, sombre ? "sombre" : "clair");
        try (var out = new FileOutputStream(FICHIER_PREFS)) {
            props.store(out, "Preferences Port de Douala");
        } catch (IOException ignored) {
        }
    }
}
