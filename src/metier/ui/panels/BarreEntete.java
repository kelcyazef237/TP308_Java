package metier.ui.panels;

import metier.ui.icons.IconeLucide;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Barre d'entete (header) en haut de la fenetre principale.
 * 60px, fond surface, 1px bottom border.
 * Gauche : titre de la page + sous-titre (breadcrumb).
 * Droite : champ de recherche + bouton raccourci Ctrl+K.
 */
public class BarreEntete extends JPanel {

    private final JLabel lblTitre;
    private final JLabel lblSousTitre;
    private final JTextField champRecherche;

    private Consumer<String> onRecherche;

    public BarreEntete() {
        Theme t = GestionnaireTheme.actif();
        setOpaque(false);
        setPreferredSize(new Dimension(0, 60));
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(0, 24, 0, 16));

        // ===== GAUCHE : titre + sous-titre =====
        JPanel gauche = new JPanel();
        gauche.setOpaque(false);
        gauche.setLayout(new BoxLayout(gauche, BoxLayout.Y_AXIS));
        gauche.setBorder(new EmptyBorder(8, 0, 8, 0));

        lblTitre = new JLabel("Overview");
        lblTitre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblTitre.setForeground(t.textePrimaire());
        lblTitre.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSousTitre = new JLabel("Port Autonome de Douala");
        lblSousTitre.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblSousTitre.setForeground(t.texteAtténué());
        lblSousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSousTitre.setBorder(new EmptyBorder(2, 0, 0, 0));

        gauche.add(lblTitre);
        gauche.add(lblSousTitre);
        add(gauche, BorderLayout.WEST);

        // ===== DROITE : recherche + bouton palette =====
        JPanel droite = new JPanel();
        droite.setOpaque(false);
        droite.setLayout(new BoxLayout(droite, BoxLayout.X_AXIS));
        droite.setBorder(new EmptyBorder(12, 0, 12, 0));

        // Champ de recherche
        JPanel rechercheWrap = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                g2.setColor(tt.surfaceElevee());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(tt.bordure());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rechercheWrap.setOpaque(false);
        rechercheWrap.setPreferredSize(new Dimension(280, 36));
        rechercheWrap.setMaximumSize(new Dimension(280, 36));
        rechercheWrap.setBorder(new EmptyBorder(0, 12, 0, 8));

        JLabel lblIconeRecherche = new JLabel(IconeLucide.de("search", 14,
            () -> GestionnaireTheme.actif().texteAtténué()));
        rechercheWrap.add(lblIconeRecherche, BorderLayout.WEST);

        champRecherche = new JTextField();
        champRecherche.setOpaque(false);
        champRecherche.setBorder(BorderFactory.createEmptyBorder());
        champRecherche.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        champRecherche.setForeground(t.textePrimaire());
        champRecherche.putClientProperty("JTextField.placeholderText", "Rechercher une cargaison...");
        champRecherche.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (onRecherche != null) onRecherche.accept(champRecherche.getText());
            }
        });
        rechercheWrap.add(champRecherche, BorderLayout.CENTER);

        JLabel lblRaccourci = new JLabel("\u2318K");
        lblRaccourci.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        lblRaccourci.setForeground(t.texteAtténué());
        rechercheWrap.add(lblRaccourci, BorderLayout.EAST);

        droite.add(rechercheWrap);

        add(droite, BorderLayout.EAST);

        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            lblTitre.setForeground(tt.textePrimaire());
            lblSousTitre.setForeground(tt.texteAtténué());
            champRecherche.setForeground(tt.textePrimaire());
            repaint();
        });
    }

    public void setTitre(String titre, String sous) {
        lblTitre.setText(titre);
        lblSousTitre.setText(sous);
    }

    public void setOnRecherche(Consumer<String> r) {
        this.onRecherche = r;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme t = GestionnaireTheme.actif();
        g2.setColor(t.surface());
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Bottom border
        g2.setColor(t.bordure());
        g2.fillRect(0, getHeight() - 1, getWidth(), 1);
        g2.dispose();
        super.paintComponent(g);
    }
}
