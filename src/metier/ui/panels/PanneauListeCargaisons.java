package metier.ui.panels;

import metier.*;
import metier.persistance.GestionDonnees;
import metier.ui.models.ModeleTableCargaison;
import metier.ui.renderers.RenduTableauCargaison;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panneau affichant le tableau des cargaisons avec barre de recherche,
 * filtre et barre d'outils au design Linear/Vercel.
 */
public class PanneauListeCargaisons extends JPanel {

    private JTable table;
    private ModeleTableCargaison tableModel;
    private TableRowSorter<ModeleTableCargaison> sorter;
    private JTextField champRecherche;
    private final GestionDonnees gestionDonnees;
    private Consumer<Marchandise> onVoirDetail;
    private JComboBox<String> filtreType;
    private JLabel lblNombreResultats;

    public PanneauListeCargaisons(GestionDonnees gestionDonnees) {
        this.gestionDonnees = gestionDonnees;
        Theme t = GestionnaireTheme.actif();
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 28, 20, 28));

        // ==== En-tete ====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titrePanel = new JPanel();
        titrePanel.setOpaque(false);
        titrePanel.setLayout(new BoxLayout(titrePanel, BoxLayout.Y_AXIS));

        JLabel titre = new JLabel("Cargaisons");
        titre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titre.setForeground(t.textePrimaire());
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblNombreResultats = new JLabel("");
        lblNombreResultats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblNombreResultats.setForeground(t.texteAtténué());
        lblNombreResultats.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNombreResultats.setBorder(new EmptyBorder(2, 0, 0, 0));

        titrePanel.add(titre);
        titrePanel.add(lblNombreResultats);
        header.add(titrePanel, BorderLayout.WEST);

        // ===== Barre d'outils visible =====
        JPanel outils = new JPanel();
        outils.setOpaque(false);
        outils.setLayout(new BoxLayout(outils, BoxLayout.X_AXIS));
        outils.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        // Filtre par categorie (conforme cahier des charges initial)
        filtreType = creerComboFiltre();
        filtreType.addActionListener(e -> appliquerFiltre());

        JButton btnAjouter = creerBoutonOutil("Ajouter");
        JButton btnSauvegarder = creerBoutonOutil("Sauvegarder");

        btnAjouter.addActionListener(e -> { if (onAjouter != null) onAjouter.run(); });
        btnSauvegarder.addActionListener(e -> { if (onSauvegarder != null) onSauvegarder.run(); });

        JLabel lblFiltre = new JLabel("Filtrer");
        lblFiltre.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblFiltre.setForeground(t.texteSecondaire());
        outils.add(lblFiltre);
        outils.add(Box.createHorizontalStrut(6));
        outils.add(filtreType);
        outils.add(Box.createHorizontalStrut(16));
        outils.add(btnAjouter);
        outils.add(Box.createHorizontalStrut(8));
        outils.add(btnSauvegarder);

        header.add(outils, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ==== Tableau ====
        tableModel = new ModeleTableCargaison();
        table = new JTable(tableModel);
        configurerTable();

        JPanel carteTableau = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                g2.setColor(tt.surface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(tt.bordure());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        carteTableau.setOpaque(false);
        carteTableau.setBorder(new EmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        carteTableau.add(scrollPane, BorderLayout.CENTER);

        add(carteTableau, BorderLayout.CENTER);

        // Theme change listener for light/dark mode
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            titre.setForeground(tt.textePrimaire());
            lblNombreResultats.setForeground(tt.texteAtténué());
            lblFiltre.setForeground(tt.texteSecondaire());
            filtreType.setForeground(tt.textePrimaire());
            filtreType.setBackground(tt.surface());
            btnAjouter.setForeground(tt.textePrimaire());
            btnSauvegarder.setForeground(tt.textePrimaire());
            table.setBackground(tt.surface());
            table.setForeground(tt.textePrimaire());
            table.setGridColor(tt.bordure());
            table.setSelectionBackground(tt.selectionBg());
            table.setSelectionForeground(tt.selectionFg());
            table.repaint();
            if (table.getTableHeader() != null) table.getTableHeader().repaint();
            carteTableau.repaint();
            repaint();
        });
    }

    public void setOnVoirDetail(Consumer<Marchandise> onVoirDetail) {
        this.onVoirDetail = onVoirDetail;
    }

    private void configurerTable() {
        Theme t = GestionnaireTheme.actif();
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setBorder(new EmptyBorder(0, 0, 0, 0));
        table.setRowMargin(0);
        table.setBackground(t.surface());
        table.setForeground(t.textePrimaire());
        table.setGridColor(t.bordure());
        table.setSelectionBackground(t.selectionBg());
        table.setSelectionForeground(t.selectionFg());

        // En-tete
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                Theme tt = GestionnaireTheme.actif();
                l.setBackground(tt.surfaceElevee());
                l.setForeground(tt.texteAtténué());
                l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, tt.bordure()),
                    new EmptyBorder(12, 16, 12, 16)
                ));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setOpaque(true);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // Rendu
        RenduTableauCargaison rendu = new RenduTableauCargaison();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rendu);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Tri numerique poids/taxe
        sorter.setComparator(3, (a, b) -> {
            if (a instanceof Double da && b instanceof Double db) return da.compareTo(db);
            return 0;
        });
        sorter.setComparator(9, (a, b) -> {
            if (a instanceof Double da && b instanceof Double db) return da.compareTo(db);
            return 0;
        });

        // Simple clic -> ouvrir detail
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && onVoirDetail != null) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Marchandise m = tableModel.getCargaisonAt(table.convertRowIndexToModel(row));
                        if (m != null) onVoirDetail.accept(m);
                    }
                }
            }
        });

        // Largeurs
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(95);
        table.getColumnModel().getColumn(9).setPreferredWidth(110);
        table.getColumnModel().getColumn(10).setPreferredWidth(110);
    }

    public Marchandise getCargaisonSelectionnee() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getCargaisonAt(table.convertRowIndexToModel(row));
    }

    public void actualiser() {
        List<Marchandise> toutes = gestionDonnees.getToutes();
        tableModel.actualiser(toutes);
        int n = table.getRowCount();
        lblNombreResultats.setText(n + " cargaison" + (n > 1 ? "s" : ""));
    }

    private String texteRecherche = "";

    // Callbacks
    private Runnable onAjouter;
    private Runnable onModifier;
    private Runnable onSupprimer;
    private Runnable onSauvegarder;
    private Runnable onCharger;

    public void setOnAjouter(Runnable r) { this.onAjouter = r; }
    public void setOnModifier(Runnable r) { this.onModifier = r; }
    public void setOnSupprimer(Runnable r) { this.onSupprimer = r; }
    public void setOnSauvegarder(Runnable r) { this.onSauvegarder = r; }
    public void setOnCharger(Runnable r) { this.onCharger = r; }

    public void appliquerRecherche(String texte) {
        this.texteRecherche = texte;
        appliquerFiltre();
    }

    private void appliquerFiltre() {
        String critere = (String) filtreType.getSelectedItem();
        String texte = this.texteRecherche != null ? this.texteRecherche.trim() : "";

        List<RowFilter<Object, Object>> filtres = new java.util.ArrayList<>();

        // Filtre texte
        if (!texte.isEmpty()) {
            filtres.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texte), 0, 2, 4, 5, 7));
        }

        // Filtre categorie
        if (critere != null) {
            switch (critere) {
                case "Standard" -> filtres.add(RowFilter.regexFilter("Standard", 1));
                case "Refrigere" -> filtres.add(RowFilter.regexFilter("Refrigere", 1));
                case "En alerte" -> filtres.add(new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends Object, ? extends Object> entry) {
                        int modelRow = (Integer) entry.getIdentifier();
                        Marchandise m = tableModel.getCargaisonAt(modelRow);
                        return m instanceof ConteneurRefrigere cr && cr.estEnAlerte();
                    }
                });
            }
        }

        if (filtres.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filtres));
        }

        int n = table.getRowCount();
        lblNombreResultats.setText(n + " cargaison" + (n > 1 ? "s" : "") + (texte.isEmpty() && (critere == null || critere.equals("Tous")) ? "" : " trouvee" + (n > 1 ? "s" : "")));
    }

    public void setOnRecherche(Consumer<String> c) { /* hook pour synchronisation future */ }

    private JButton creerBoutonOutil(String texte) {
        Theme t = GestionnaireTheme.actif();
        JButton b = new JButton(texte) {
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
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        b.setForeground(t.textePrimaire());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 32));
        b.setMaximumSize(new Dimension(130, 32));
        b.setMinimumSize(new Dimension(80, 32));
        return b;
    }

    private JComboBox<String> creerComboFiltre() {
        Theme t = GestionnaireTheme.actif();
        JComboBox<String> combo = new JComboBox<>(new String[]{"Tous", "Standard", "Refrigere", "En alerte"});
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        combo.setForeground(t.textePrimaire());
        combo.setBackground(t.surface());
        combo.setPreferredSize(new Dimension(130, 32));
        combo.setMaximumSize(new Dimension(150, 32));
        return combo;
    }
}
