package metier.ui.panels;

import metier.*;
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
 * Panneau de detail qui glisse depuis la droite (480px de large).
 * Animation : slide-in / slide-out sur 250ms via javax.swing.Timer.
 * S'affiche en superposition (JLayeredPane) sur le contenu principal.
 */
public class PanneauDetailGlissant extends JPanel {

    public static final int LARGEUR = 480;
    private static final int DUREE_ANIM_MS = 250;
    private static final int INTERVALLE_MS = 12;

    private Marchandise marchandise;
    private JPanel contenuInterne;
    private Timer timerAnimation;
    private long debutAnim;
    private int xCible;
    private boolean enEntree;
    private boolean estVisibleLogique = false;

    private Consumer<Marchandise> onModifier;
    private Consumer<Marchandise> onSupprimer;
    private Runnable onFermetureComplete;

    public PanneauDetailGlissant() {
        Theme t = GestionnaireTheme.actif();
        setOpaque(true);
        setBackground(t.bg());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(LARGEUR, 0));

        contenuInterne = new JPanel();
        contenuInterne.setOpaque(false);
        contenuInterne.setLayout(new BoxLayout(contenuInterne, BoxLayout.Y_AXIS));
        contenuInterne.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(contenuInterne);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            setBackground(tt.bg());
            if (marchandise != null) {
                construireContenu();
            }
            contenuInterne.repaint();
        });
    }

    public boolean estVisibleLogique() { return estVisibleLogique; }

    public void setOnModifier(Consumer<Marchandise> r) { this.onModifier = r; }
    public void setOnSupprimer(Consumer<Marchandise> r) { this.onSupprimer = r; }
    public void setOnFermetureComplete(Runnable r) { this.onFermetureComplete = r; }

    /**
     * Affiche le detail de la marchandise en glissant depuis la droite.
     * @parentWidth largeur du parent (pour calculer l'arrivee)
     */
    public void afficher(Marchandise m, int parentWidth) {
        this.marchandise = m;
        construireContenu();
        estVisibleLogique = true;
        // Position de depart : a droite du parent
        setSize(LARGEUR, getHeight() > 0 ? getHeight() : 600);
        setLocation(parentWidth, 0);
        xCible = parentWidth - LARGEUR;
        setVisible(true);
        demarrerAnimation(true);
    }

    public void masquer(int parentWidth) {
        if (!estVisibleLogique) return;
        xCible = parentWidth;
        demarrerAnimation(false);
    }

    private void demarrerAnimation(boolean entree) {
        this.enEntree = entree;
        if (timerAnimation != null && timerAnimation.isRunning()) {
            timerAnimation.stop();
        }
        debutAnim = System.currentTimeMillis();
        timerAnimation = new Timer(INTERVALLE_MS, e -> animer());
        timerAnimation.start();
    }

    private void animer() {
        long elapsed = System.currentTimeMillis() - debutAnim;
        float t = Math.min(1f, (float) elapsed / DUREE_ANIM_MS);
        // Ease-in-out cubic
        float eased = enEntree
            ? easeOutCubic(t)
            : easeInCubic(t);
        int startX = enEntree ? getParent().getWidth() : xCible - LARGEUR;
        int x;
        if (enEntree) {
            x = getParent().getWidth() - (int) (eased * LARGEUR);
        } else {
            x = (getParent().getWidth() - LARGEUR) + (int) (eased * LARGEUR);
        }
        setLocation(x, 0);
        if (t >= 1f) {
            timerAnimation.stop();
            timerAnimation = null;
            if (!enEntree) {
                estVisibleLogique = false;
                setVisible(false);
                if (onFermetureComplete != null) {
                    SwingUtilities.invokeLater(onFermetureComplete);
                }
            }
        }
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    private void construireContenu() {
        contenuInterne.removeAll();
        Theme t = GestionnaireTheme.actif();

        // ==== En-tete ====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 24, 16, 24));

        // Bouton retour
        JButton btnRetour = new JButton(IconeLucide.de("arrow-left", 18, () -> t.texteSecondaire())) {
            private boolean survol = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { survol = true; repaint(); }
                    public void mouseExited(MouseEvent e) { survol = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (survol) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Theme tt = GestionnaireTheme.actif();
                    g2.setColor(tt.surfaceElevee());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btnRetour.setPreferredSize(new Dimension(32, 32));
        btnRetour.setFocusPainted(false);
        btnRetour.setBorderPainted(false);
        btnRetour.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRetour.setToolTipText("Fermer");
        btnRetour.addActionListener(e -> {
            if (getParent() != null) {
                masquer(getParent().getWidth());
            }
        });
        header.add(btnRetour, BorderLayout.WEST);

        // Indicateur de type
        JLabel lblType = new JLabel();
        if (marchandise instanceof ConteneurRefrigere) {
            lblType.setText("REFRIGERE");
            lblType.setForeground(new Color(0x56, 0xC2, 0xE0));
        } else {
            lblType.setText("STANDARD");
            lblType.setForeground(t.accent());
        }
        lblType.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        JPanel typeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        typeWrap.setOpaque(false);
        typeWrap.add(lblType);
        header.add(typeWrap, BorderLayout.EAST);

        contenuInterne.add(header);

        // ==== Titre / numero ====
        JPanel titrePanel = new JPanel();
        titrePanel.setOpaque(false);
        titrePanel.setLayout(new BoxLayout(titrePanel, BoxLayout.Y_AXIS));
        titrePanel.setBorder(new EmptyBorder(0, 24, 16, 24));

        JLabel lblNumero = new JLabel(marchandise.getNumeroConteneur());
        lblNumero.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        lblNumero.setForeground(t.textePrimaire());
        lblNumero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel(marchandise.getDescription());
        lblDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        lblDesc.setForeground(t.texteSecondaire());
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setBorder(new EmptyBorder(4, 0, 0, 0));

        titrePanel.add(lblNumero);
        titrePanel.add(lblDesc);
        contenuInterne.add(titrePanel);

        // ==== Statut badge ====
        JPanel statutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statutPanel.setOpaque(false);
        statutPanel.setBorder(new EmptyBorder(0, 24, 20, 24));
        statutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statutPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblStatut = creerBadgeStatut(marchandise.getStatut(), t);
        statutPanel.add(lblStatut);
        contenuInterne.add(statutPanel);

        // ==== Section informations generales ====
        contenuInterne.add(creerSection("Informations generales", t));
        contenuInterne.add(creerChamp("Poids", String.format("%,.0f kg", marchandise.getPoids()), t));
        contenuInterne.add(creerChamp("Origine", marchandise.getOrigine(), t));
        contenuInterne.add(creerChamp("Destination", marchandise.getDestination(), t));
        contenuInterne.add(creerChamp("Compagnie", marchandise.getCompagnieMaritime(), t));
        contenuInterne.add(creerChamp("Date d'arrivee",
            marchandise.getDateArrivee() != null ? marchandise.getDateArrivee().toString() : "-", t));

        // ==== Section refrigerateur ====
        if (marchandise instanceof ConteneurRefrigere cr) {
            contenuInterne.add(Box.createVerticalStrut(8));
            contenuInterne.add(creerSection("Conditions de refrigeration", t));
            contenuInterne.add(creerChamp("Temperature actuelle",
                String.format("%.1f \u00B0C", cr.getTemperatureActuelle()), t));
            contenuInterne.add(creerChamp("Plage acceptable",
                String.format("%.1f \u00B0C a %.1f \u00B0C", cr.getTemperatureMin(), cr.getTemperatureMax()), t));
            contenuInterne.add(creerChamp("Etat", cr.estEnAlerte() ? "EN ALERTE" : "Nominal",
                cr.estEnAlerte() ? t.danger() : t.succes(), t));
        } else if (marchandise instanceof ConteneurStandard cs) {
            contenuInterne.add(Box.createVerticalStrut(8));
            contenuInterne.add(creerSection("Contenu", t));
            contenuInterne.add(creerChamp("Description", cs.getContenu(), t));
        }

        // ==== Section financiere ====
        contenuInterne.add(Box.createVerticalStrut(8));
        contenuInterne.add(creerSection("Finances", t));
        contenuInterne.add(creerChamp("Taxe portuaire",
            String.format("%,.0f FCFA", marchandise.calculerTaxe()), t));

        // ==== Actions ====
        contenuInterne.add(Box.createVerticalStrut(20));
        JPanel actionsPanel = new JPanel();
        actionsPanel.setOpaque(false);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBorder(new EmptyBorder(0, 24, 24, 24));
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // First row: Modifier + Supprimer
        JPanel rangee1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rangee1.setOpaque(false);
        rangee1.setAlignmentX(Component.LEFT_ALIGNMENT);
        rangee1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnModifier = new JButton("Modifier") {
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
                g2.setColor(survol ? tt.accentHover() : tt.accent());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btnModifier.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btnModifier.setForeground(Color.WHITE);
        btnModifier.setFocusPainted(false);
        btnModifier.setBorderPainted(false);
        btnModifier.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModifier.setPreferredSize(new Dimension(140, 42));
        btnModifier.setToolTipText("Editer cette cargaison");
        btnModifier.addActionListener(e -> {
            if (onModifier != null && marchandise != null) onModifier.accept(marchandise);
        });
        rangee1.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer") {
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
                g2.setColor(survol ? new Color(0xC0, 0x3B, 0x3B) : tt.danger());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btnSupprimer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.setBorderPainted(false);
        btnSupprimer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSupprimer.setPreferredSize(new Dimension(140, 42));
        btnSupprimer.setToolTipText("Supprimer cette cargaison");
        btnSupprimer.addActionListener(e -> {
            if (onSupprimer != null && marchandise != null) onSupprimer.accept(marchandise);
        });
        rangee1.add(btnSupprimer);

        actionsPanel.add(rangee1);
        actionsPanel.add(Box.createVerticalStrut(8));

        // Second row: Fermer
        JPanel rangee2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rangee2.setOpaque(false);
        rangee2.setAlignmentX(Component.LEFT_ALIGNMENT);
        rangee2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnFermer = new JButton("Fermer") {
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
                Color bordure = tt.bordure();
                g2.setColor(new Color(bordure.getRed(), bordure.getGreen(), bordure.getBlue(), survol ? 255 : 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btnFermer.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btnFermer.setForeground(t.textePrimaire());
        btnFermer.setFocusPainted(false);
        btnFermer.setBorderPainted(false);
        btnFermer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFermer.setPreferredSize(new Dimension(140, 42));
        btnFermer.setToolTipText("Fermer le panneau");
        btnFermer.addActionListener(e -> {
            if (getParent() != null) masquer(getParent().getWidth());
        });
        rangee2.add(btnFermer);

        actionsPanel.add(rangee2);

        contenuInterne.add(actionsPanel);

        contenuInterne.revalidate();
        contenuInterne.repaint();
    }

    private JLabel creerBadgeStatut(String statut, Theme t) {
        Color couleur = switch (statut != null ? statut.toLowerCase() : "") {
            case "arrivee", "livre" -> t.succes();
            case "en transit" -> t.info();
            case "en attente" -> t.avertissement();
            case "alerte" -> t.danger();
            case "inspection" -> new Color(0x9B, 0x59, 0xB6);
            default -> t.texteSecondaire();
        };
        JLabel lbl = new JLabel(statut != null ? statut.toUpperCase() : "");
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        lbl.setForeground(couleur);
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 100), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 30));
        return lbl;
    }

    private JPanel creerSection(String titre, Theme t) {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(8, 24, 6, 24));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel lbl = new JLabel(titre.toUpperCase());
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        lbl.setForeground(t.texteAtténué());
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, t.bordure()));
        section.add(lbl, BorderLayout.CENTER);
        return section;
    }

    private JPanel creerChamp(String label, String valeur, Theme t) {
        return creerChamp(label, valeur, t.textePrimaire(), t);
    }

    private JPanel creerChamp(String label, String valeur, Color couleurValeur, Theme t) {
        JPanel ligne = new JPanel(new BorderLayout(12, 0));
        ligne.setOpaque(false);
        ligne.setBorder(new EmptyBorder(6, 24, 6, 24));
        ligne.setAlignmentX(Component.LEFT_ALIGNMENT);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lbl.setForeground(t.texteSecondaire());
        lbl.setPreferredSize(new Dimension(140, 20));

        JLabel lblVal = new JLabel(valeur);
        lblVal.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblVal.setForeground(couleurValeur);
        if (couleurValeur == t.danger() || couleurValeur == t.succes()) {
            lblVal.setFont(lblVal.getFont().deriveFont(Font.BOLD));
        }

        ligne.add(lbl, BorderLayout.WEST);
        ligne.add(lblVal, BorderLayout.CENTER);
        return ligne;
    }
}
