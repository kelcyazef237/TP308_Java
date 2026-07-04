package metier.ui.panels;

import metier.ui.icons.IconeLucide;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Barre laterale textuelle claire.
 * Largeur 180px, sections bien ordonnees :
 *   - Vue d'ensemble
 *   - Cargaisons
 *   - Sauvegarder
 *   - Charger
 *   - Theme sombre/clair
 *   - Indicateur "Systeme actif"
 */
public class BarreLaterale extends JPanel {

    public static final String VUE_TABLEAU_BORD = "TABLEAU_DE_BORD";
    public static final String VUE_CARGAISONS = "CARGAISONS";

    public static final int LARGEUR = 180;

    private final Consumer<String> onNavigation;
    private final List<JButton> boutons = new ArrayList<>();
    private JButton boutonActif;
    private final JButton boutonTheme;
    private final JLabel lblEnLigne;

    public BarreLaterale(Consumer<String> onNavigation) {
        this.onNavigation = onNavigation;
        Theme t = GestionnaireTheme.actif();

        setOpaque(false);
        setPreferredSize(new Dimension(LARGEUR, 0));
        setMinimumSize(new Dimension(LARGEUR, 0));
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ===== HAUT : logo + navigation =====
        JPanel haut = new JPanel();
        haut.setOpaque(false);
        haut.setLayout(new BoxLayout(haut, BoxLayout.Y_AXIS));
        haut.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));

        // Titre app
        JLabel lblMarque = new JLabel("Port de Douala");
        lblMarque.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblMarque.setForeground(t.accent());
        lblMarque.setAlignmentX(Component.LEFT_ALIGNMENT);
        haut.add(lblMarque);

        JLabel lblSousMarque = new JLabel("Gestion du fret");
        lblSousMarque.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblSousMarque.setForeground(t.texteAtténué());
        lblSousMarque.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSousMarque.setBorder(BorderFactory.createEmptyBorder(2, 0, 20, 0));
        haut.add(lblSousMarque);

        // Separateur
        haut.add(creerSeparateur(t));

        // Navigation
        boutonActif = ajouterBouton(haut, "Vue d'ensemble", VUE_TABLEAU_BORD, true);
        ajouterBouton(haut, "Cargaisons", VUE_CARGAISONS, false);

        haut.add(Box.createVerticalStrut(18));
        haut.add(creerSeparateur(t));

        // Actions
        ajouterBouton(haut, "Sauvegarder", "SAUVEGARDER", false);
        ajouterBouton(haut, "Charger", "CHARGER", false);

        haut.add(Box.createVerticalGlue());
        add(haut, BorderLayout.CENTER);

        // ===== BAS : theme + online =====
        JPanel bas = new JPanel();
        bas.setOpaque(false);
        bas.setLayout(new BoxLayout(bas, BoxLayout.Y_AXIS));
        bas.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        bas.add(creerSeparateur(t));
        bas.add(Box.createVerticalStrut(10));

        // Bouton theme
        boutonTheme = new JButton(GestionnaireTheme.estSombre() ? "Mode clair" : "Mode sombre") {
            private boolean survol = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { survol = true; repaint(); }
                    public void mouseExited(MouseEvent e) { survol = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                g2.setColor(survol ? tt.surfaceElevee() : tt.surface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(tt.bordure());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        boutonTheme.setFocusPainted(false);
        boutonTheme.setBorderPainted(false);
        boutonTheme.setContentAreaFilled(false);
        boutonTheme.setOpaque(false);
        boutonTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boutonTheme.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        boutonTheme.setForeground(t.textePrimaire());
        boutonTheme.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        boutonTheme.setPreferredSize(new Dimension(140, 36));
        boutonTheme.setAlignmentX(Component.LEFT_ALIGNMENT);
        boutonTheme.addActionListener(e -> {
            if (onNavigation != null) onNavigation.accept("THEME");
        });
        bas.add(boutonTheme);

        bas.add(Box.createVerticalStrut(12));

        // Indicateur en ligne
        lblEnLigne = new JLabel("Systeme actif");
        lblEnLigne.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblEnLigne.setForeground(t.succes());
        lblEnLigne.setIcon(IconeLucide.de("circle-dot", 10, () -> GestionnaireTheme.actif().succes()));
        lblEnLigne.setAlignmentX(Component.LEFT_ALIGNMENT);
        bas.add(lblEnLigne);

        add(bas, BorderLayout.SOUTH);

        // Mise a jour auto quand le theme change
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            lblMarque.setForeground(tt.accent());
            lblSousMarque.setForeground(tt.texteAtténué());
            boutonTheme.setText(GestionnaireTheme.estSombre() ? "Mode clair" : "Mode sombre");
            boutonTheme.setForeground(tt.textePrimaire());
            lblEnLigne.setForeground(tt.succes());
            for (JButton b : boutons) {
                b.setForeground(tt.texteSecondaire());
                b.repaint();
            }
            if (boutonActif != null) boutonActif.setForeground(tt.accent());
            repaint();
        });
    }

    private JButton ajouterBouton(JPanel container, String texte, String identifiantVue, boolean estDefaut) {
        JButton bouton = new JButton(texte) {
            private boolean survol = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { survol = true; repaint(); }
                    public void mouseExited(MouseEvent e) { survol = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                boolean actif = BarreLaterale.this.boutonActif == this;
                if (actif || survol) {
                    g2.setColor(tt.surfaceElevee());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                // Petit indicateur accent a gauche si actif
                if (actif) {
                    g2.setColor(tt.accent());
                    g2.fillRoundRect(0, getHeight()/2 - 8, 3, 16, 2, 2);
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setContentAreaFilled(false);
        bouton.setOpaque(false);
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bouton.setHorizontalAlignment(SwingConstants.LEFT);
        bouton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bouton.setPreferredSize(new Dimension(140, 36));
        bouton.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme t = GestionnaireTheme.actif();
        bouton.setForeground(estDefaut ? t.accent() : t.texteSecondaire());
        bouton.addActionListener(e -> {
            setBoutonActif(bouton);
            if (onNavigation != null) onNavigation.accept(identifiantVue);
        });
        boutons.add(bouton);
        container.add(bouton);
        container.add(Box.createVerticalStrut(4));
        if (estDefaut) {
            setBoutonActif(bouton);
        }
        return bouton;
    }

    private JPanel creerSeparateur(Theme t) {
        JPanel sep = new JPanel();
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, t.bordure()));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    public void setBoutonActif(JButton bouton) {
        Theme t = GestionnaireTheme.actif();
        if (boutonActif != null) {
            boutonActif.setForeground(t.texteSecondaire());
        }
        boutonActif = bouton;
        if (boutonActif != null) {
            boutonActif.setForeground(t.accent());
        }
        repaint();
    }

    public void setConteneurActif(String identifiant) {
        // Non utilise avec les labels textuels, garde pour compatibilite
    }

    public void setSurActionTheme(Runnable r) {
        boutonTheme.addActionListener(e -> r.run());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Theme t = GestionnaireTheme.actif();
        g2.setColor(t.surface());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(t.bordure());
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
