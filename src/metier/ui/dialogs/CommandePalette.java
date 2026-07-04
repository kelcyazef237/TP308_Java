package metier.ui.dialogs;

import metier.ui.icons.IconeLucide;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Palette de commandes au style Linear/Vercel : 560px de large, jusqu'a 380px de haut,
 * ancree en haut de la fenetre proprietaire. S'ouvre avec Ctrl+K.
 *
 * <p>Recherche incrementale (substring, case-insensitive) sur la liste des commandes.</p>
 *
 * <p>Navigation clavier :</p>
 * <ul>
 *   <li>Haut / Bas : changer la selection</li>
 *   <li>Entree : executer la commande selectionnee</li>
 *   <li>Escape : fermer</li>
 * </ul>
 */
public class CommandePalette extends JDialog {

    /** Une commande executable dans la palette. */
    public record Commande(String libelle, String description, String nomIcone, Runnable action, String raccourci) {}

    private final JTextField champRecherche;
    private final JList<Commande> liste;
    private final DefaultListModel<Commande> modele;
    private final JLabel lblFooter;
    private final List<Commande> toutesLesCommandes;
    private final Consumer<Commande> surSelection;
    private final JFrame parent;

    public CommandePalette(JFrame parent, List<Commande> commandes, Consumer<Commande> surSelection) {
        super(parent, false);
        this.parent = parent;
        this.toutesLesCommandes = new ArrayList<>(commandes);
        this.surSelection = surSelection;

        setUndecorated(true);
        setSize(560, 380);
        setAlwaysOnTop(true);

        Theme t = GestionnaireTheme.actif();
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                int w = getWidth();
                int h = getHeight();
                int arc = 12;
                // Ombre
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(2, 4, w - 4, h - 4, arc, arc);
                // Fond
                g2.setColor(tt.surface());
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // Bordure
                g2.setColor(tt.bordureForte());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ==== Zone de recherche ====
        JPanel haut = new JPanel(new BorderLayout(10, 0));
        haut.setOpaque(false);
        haut.setBorder(new EmptyBorder(16, 18, 12, 18));

        JLabel lblIcone = new JLabel(IconeLucide.de("search", 16,
            () -> GestionnaireTheme.actif().texteAtténué()));
        haut.add(lblIcone, BorderLayout.WEST);

        champRecherche = new JTextField();
        champRecherche.setOpaque(false);
        champRecherche.setBorder(BorderFactory.createEmptyBorder());
        champRecherche.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        champRecherche.setForeground(t.textePrimaire());
        champRecherche.setCaretColor(t.accent());
        champRecherche.putClientProperty("JTextField.placeholderText", "Tapez une commande...");
        champRecherche.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    fermer();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executerSelection();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    deplacerSelection(1);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    deplacerSelection(-1);
                } else {
                    filtrer();
                }
            }
        });
        haut.add(champRecherche, BorderLayout.CENTER);

        root.add(haut, BorderLayout.NORTH);

        // ==== Liste ====
        modele = new DefaultListModel<>();
        for (Commande c : toutesLesCommandes) modele.addElement(c);

        liste = new JList<>(modele) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        liste.setCellRenderer(new RenduCommande());
        liste.setOpaque(false);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setSelectedIndex(0);
        liste.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) executerSelection();
            }
        });
        liste.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executerSelection();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    fermer();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(liste);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);

        // ==== Footer ====
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 18, 12, 18));
        JLabel lblSep = new JLabel();
        lblSep.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, t.bordure()));
        footer.add(lblSep, BorderLayout.NORTH);

        lblFooter = new JLabel("\u2191\u2193 naviguer  \u00B7  \u21B5 selectionner  \u00B7  esc fermer");
        lblFooter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        lblFooter.setForeground(t.texteAtténué());
        footer.add(lblFooter, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Affiche la palette ancree en haut de la fenetre proprietaire. */
    public void afficher() {
        if (parent == null) return;
        Rectangle bounds = parent.getBounds();
        int x = bounds.x + (bounds.width - getWidth()) / 2;
        int y = bounds.y + 80;
        setLocation(x, y);
        champRecherche.setText("");
        filtrer();
        setVisible(true);
        champRecherche.requestFocusInWindow();
    }

    public void fermer() {
        setVisible(false);
        dispose();
    }

    private void filtrer() {
        String q = champRecherche.getText().trim().toLowerCase(Locale.ROOT);
        modele.clear();
        for (Commande c : toutesLesCommandes) {
            if (q.isEmpty()
                || c.libelle().toLowerCase(Locale.ROOT).contains(q)
                || c.description().toLowerCase(Locale.ROOT).contains(q)) {
                modele.addElement(c);
            }
        }
        if (!modele.isEmpty()) {
            liste.setSelectedIndex(0);
        }
    }

    private void deplacerSelection(int delta) {
        int n = modele.size();
        if (n == 0) return;
        int i = liste.getSelectedIndex();
        int nv = (i + delta + n) % n;
        liste.setSelectedIndex(nv);
        liste.ensureIndexIsVisible(nv);
    }

    private void executerSelection() {
        Commande c = liste.getSelectedValue();
        if (c == null) return;
        Runnable action = c.action();
        if (surSelection != null) surSelection.accept(c);
        fermer();
        if (action != null) {
            SwingUtilities.invokeLater(action);
        }
    }

    /** Renderer d'un item : icone + libelle/description + raccourci a droite. */
    private static class RenduCommande extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Theme t = GestionnaireTheme.actif();
            l.setOpaque(false);
            l.setBorder(new EmptyBorder(8, 14, 8, 14));
            if (!(value instanceof Commande c)) return l;
            l.setLayout(new BorderLayout(10, 0));

            // Icone
            JLabel lblIcone = new JLabel(IconeLucide.de(c.nomIcone(), 16,
                () -> isSelected ? t.selectionFg() : t.texteSecondaire()));
            l.add(lblIcone, BorderLayout.WEST);

            // Centre : libelle + description
            JPanel centre = new JPanel();
            centre.setOpaque(false);
            centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
            JLabel lblLib = new JLabel(c.libelle());
            lblLib.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            lblLib.setForeground(isSelected ? t.selectionFg() : t.textePrimaire());
            lblLib.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblDesc = new JLabel(c.description());
            lblDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            lblDesc.setForeground(isSelected
                ? new Color(t.selectionFg().getRed(), t.selectionFg().getGreen(), t.selectionFg().getBlue(), 200)
                : t.texteAtténué());
            lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
            centre.add(lblLib);
            centre.add(lblDesc);
            l.add(centre, BorderLayout.CENTER);

            // Raccourci
            if (c.raccourci() != null && !c.raccourci().isEmpty()) {
                JLabel lblRacc = new JLabel(c.raccourci());
                lblRacc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                lblRacc.setForeground(isSelected ? t.selectionFg() : t.texteAtténué());
                lblRacc.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(t.bordure(), 1),
                    new EmptyBorder(2, 6, 2, 6)));
                JPanel raccourciWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
                raccourciWrap.setOpaque(false);
                raccourciWrap.add(lblRacc);
                l.add(raccourciWrap, BorderLayout.EAST);
            }

            return l;
        }
    }
}
