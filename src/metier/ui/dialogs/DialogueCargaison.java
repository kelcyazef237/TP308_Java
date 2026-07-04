package metier.ui.dialogs;

import metier.*;
import metier.ui.icons.IconeLucide;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialogue modal pour ajouter ou modifier une cargaison.
 * Design Linear/Vercel : theme-aware, icones Lucide, mise en page claire.
 */
public class DialogueCargaison extends JDialog {

    public enum Mode { AJOUTER, MODIFIER }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Marchandise resultat;
    private final Mode mode;
    private final Marchandise existant;
    private final java.util.List<Integer> idsExistants;

    private final JTextField txtNumeroConteneur;
    private final JComboBox<String> cmbType;
    private final JTextField txtCargaison;
    private final JTextField txtPoids;
    private final JTextField txtOrigine;
    private final JTextField txtDestination;
    private final JTextField txtDateArrivee;
    private final JTextField txtCompagnie;
    private final JComboBox<String> cmbStatut;
    private final JLabel lblErreur;

    private final JPanel panneauRefrigere;
    private final JTextField txtTempMin;
    private final JTextField txtTempMax;

    private final JPanel panneauStandard;
    private final JTextField txtContenu;

    public DialogueCargaison(Frame parent, String titre, Mode mode, Marchandise existant,
                              java.util.List<Integer> idsExistants) {
        super(parent, titre, true);
        this.mode = mode;
        this.existant = existant;
        this.idsExistants = idsExistants;

        setMinimumSize(new Dimension(720, 700));
        setSize(820, 720);
        setLocationRelativeTo(parent);
        setResizable(true);

        Theme t = GestionnaireTheme.actif();

        // ==== CONTENEUR PRINCIPAL ====
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(t.bg());

        // ==== EN-TETE ====
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Theme tt = GestionnaireTheme.actif();
                g2.setColor(tt.surface());
                g2.fillRect(0, 0, w, h);
                g2.setColor(tt.accent());
                g2.fillRect(0, h - 3, w, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 76));
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel titrePanel = new JPanel();
        titrePanel.setOpaque(false);
        titrePanel.setLayout(new BoxLayout(titrePanel, BoxLayout.Y_AXIS));

        JLabel lblSousTitre = new JLabel(mode == Mode.AJOUTER ? "NOUVELLE ENTREE" : "MODIFICATION");
        lblSousTitre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        lblSousTitre.setForeground(t.texteAtténué());
        lblSousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitre.setForeground(t.textePrimaire());
        lblTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitre.setBorder(new EmptyBorder(2, 0, 0, 0));

        titrePanel.add(lblSousTitre);
        titrePanel.add(lblTitre);

        header.add(titrePanel, BorderLayout.WEST);

        JLabel lblIcone = new JLabel(IconeLucide.de(
            mode == Mode.AJOUTER ? "plus" : "pencil", 28,
            () -> GestionnaireTheme.actif().accent()));
        header.add(lblIcone, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ==== CONTENU ====
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.setBackground(t.bg());
        contenu.setBorder(new EmptyBorder(20, 28, 16, 28));

        class ScrollablePanel extends JPanel implements Scrollable {
            public ScrollablePanel(LayoutManager layout) {
                super(layout);
            }
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }
            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 64;
            }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }
        ScrollablePanel formulaire = new ScrollablePanel(new GridBagLayout());
        formulaire.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;

        int ligne = 0;

        ajouterEtiquette(formulaire, gbc, "N\u00B0 Conteneur (auto) *", 0, ligne);
        txtNumeroConteneur = creerChampTexte();
        txtNumeroConteneur.setEditable(false);
        txtNumeroConteneur.setFocusable(false);
        ajouterChamp(formulaire, gbc, txtNumeroConteneur, 1, ligne);
        ajouterEtiquette(formulaire, gbc, "Type *", 2, ligne);
        cmbType = new JComboBox<>(new String[]{"Standard", "Refrigere"});
        cmbType.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cmbType.setPreferredSize(new Dimension(220, 36));
        ajouterChamp(formulaire, gbc, cmbType, 3, ligne);
        ligne++;

        ajouterEtiquette(formulaire, gbc, "Cargaison *", 0, ligne);
        txtCargaison = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtCargaison, 1, ligne);
        ajouterEtiquette(formulaire, gbc, "Compagnie *", 2, ligne);
        txtCompagnie = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtCompagnie, 3, ligne);
        ligne++;

        ajouterEtiquette(formulaire, gbc, "Poids (kg) *", 0, ligne);
        txtPoids = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtPoids, 1, ligne);
        ajouterEtiquette(formulaire, gbc, "Arrivee (jj/MM/aaaa) *", 2, ligne);
        txtDateArrivee = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtDateArrivee, 3, ligne);
        ligne++;

        ajouterEtiquette(formulaire, gbc, "Origine *", 0, ligne);
        txtOrigine = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtOrigine, 1, ligne);
        ajouterEtiquette(formulaire, gbc, "Destination *", 2, ligne);
        txtDestination = creerChampTexte();
        ajouterChamp(formulaire, gbc, txtDestination, 3, ligne);
        ligne++;

        ajouterEtiquette(formulaire, gbc, "Statut *", 0, ligne);
        cmbStatut = new JComboBox<>(new String[]{"En attente", "En transit", "Arrivee", "Inspection"});
        cmbStatut.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cmbStatut.setPreferredSize(new Dimension(200, 36));
        ajouterChamp(formulaire, gbc, cmbStatut, 1, ligne);
        ligne++;

        gbc.gridx = 0;
        gbc.gridy = ligne;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(14, 6, 6, 6);

        panneauStandard = creerCarteType("Conteneur Standard",
            () -> GestionnaireTheme.actif().accent(), "package");
        JPanel champsStandard = new JPanel(new GridBagLayout());
        champsStandard.setOpaque(false);
        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.fill = GridBagConstraints.HORIZONTAL;
        gbcS.insets = new Insets(4, 8, 4, 8);
        gbcS.weightx = 0.5;
        gbcS.anchor = GridBagConstraints.WEST;
        ajouterEtiquette(champsStandard, gbcS, "Contenu *", 0, 0);
        txtContenu = creerChampTexte();
        ajouterChamp(champsStandard, gbcS, txtContenu, 1, 0);
        panneauStandard.add(champsStandard);
        formulaire.add(panneauStandard, gbc);

        panneauRefrigere = creerCarteType("Conteneur Refrigere",
            () -> new Color(0x56, 0xC2, 0xE0), "snowflake");
        JPanel champsRefrigere = new JPanel(new GridBagLayout());
        champsRefrigere.setOpaque(false);
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.HORIZONTAL;
        gbcR.insets = new Insets(4, 8, 4, 8);
        gbcR.weightx = 0.5;
        gbcR.anchor = GridBagConstraints.WEST;
        ajouterEtiquette(champsRefrigere, gbcR, "Temp. Min (\u00B0C) *", 0, 0);
        txtTempMin = creerChampTexte();
        ajouterChamp(champsRefrigere, gbcR, txtTempMin, 1, 0);
        ajouterEtiquette(champsRefrigere, gbcR, "Temp. Max (\u00B0C) *", 0, 1);
        txtTempMax = creerChampTexte();
        ajouterChamp(champsRefrigere, gbcR, txtTempMax, 1, 1);
        panneauRefrigere.add(champsRefrigere);
        panneauRefrigere.setVisible(false);
        formulaire.add(panneauRefrigere, gbc);
        ligne++;

        cmbType.addActionListener(e -> {
            boolean estRefrigere = "Refrigere".equals(cmbType.getSelectedItem());
            panneauRefrigere.setVisible(estRefrigere);
            panneauStandard.setVisible(!estRefrigere);
            revalidate();
            repaint();
        });

        gbc.gridy = ligne;
        gbc.insets = new Insets(10, 6, 0, 6);
        lblErreur = new JLabel(" ");
        lblErreur.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        lblErreur.setForeground(t.danger());
        formulaire.add(lblErreur, gbc);

        JScrollPane scrollForm = new JScrollPane(formulaire);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.setBorder(null);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contenu.add(scrollForm, BorderLayout.CENTER);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnAnnuler = new JButton("Annuler") {
            private boolean survol = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { survol = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { survol = false; repaint(); }
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
        btnAnnuler.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnAnnuler.setForeground(t.textePrimaire());
        btnAnnuler.setBorderPainted(false);
        btnAnnuler.setContentAreaFilled(false);
        btnAnnuler.setFocusPainted(false);
        btnAnnuler.setOpaque(false);
        btnAnnuler.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnnuler.setPreferredSize(new Dimension(100, 36));
        btnAnnuler.addActionListener(e -> {
            resultat = null;
            dispose();
        });
        boutons.add(btnAnnuler);

        JButton btnOK = new JButton(mode == Mode.AJOUTER ? "Ajouter la cargaison" : "Enregistrer") {
            private boolean survol = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { survol = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { survol = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme tt = GestionnaireTheme.actif();
                g2.setColor(survol ? tt.accentHover() : tt.accent());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btnOK.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnOK.setForeground(Color.WHITE);
        btnOK.setPreferredSize(new Dimension(180, 36));
        btnOK.setBorderPainted(false);
        btnOK.setContentAreaFilled(false);
        btnOK.setFocusPainted(false);
        btnOK.setOpaque(false);
        btnOK.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOK.addActionListener(e -> {
            if (valider()) {
                resultat = construireMarchandise();
                dispose();
            }
        });
        boutons.add(btnOK);

        contenu.add(boutons, BorderLayout.SOUTH);
        root.add(contenu, BorderLayout.CENTER);
        setContentPane(root);

        if (mode == Mode.MODIFIER && existant != null) {
            preRemplir(existant);
        } else {
            txtDateArrivee.setText(LocalDate.now().format(DATE_FMT));
            txtNumeroConteneur.setText(genererProchainNumeroConteneur(idsExistants));
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                resultat = null;
            }
        });
    }

    private JTextField creerChampTexte() {
        Theme t = GestionnaireTheme.actif();
        JTextField txt = new JTextField(20);
        txt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        txt.setPreferredSize(new Dimension(220, 36));
        txt.setOpaque(true);
        txt.setBackground(t.surfaceElevee());
        txt.setForeground(t.textePrimaire());
        txt.setCaretColor(t.accent());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(t.bordure(), 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        return txt;
    }

    private void ajouterEtiquette(JPanel panneau, GridBagConstraints gbc,
                                   String etiquette, int col, int ligne) {
        Theme t = GestionnaireTheme.actif();
        JLabel lbl = new JLabel(etiquette);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lbl.setForeground(t.texteSecondaire());
        gbc.gridx = col;
        gbc.gridy = ligne;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panneau.add(lbl, gbc);
    }

    private void ajouterChamp(JPanel panneau, GridBagConstraints gbc,
                               JComponent champ, int col, int ligne) {
        gbc.gridx = col;
        gbc.gridy = ligne;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panneau.add(champ, gbc);
        gbc.weightx = 0;
    }

    private JPanel creerCarteType(String titre, java.util.function.Supplier<Color> couleur, String nomIcone) {
        JPanel carte = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = couleur.get();
                Color fond = new Color(c.getRed(), c.getGreen(), c.getBlue(), 25);
                g2.setColor(fond);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, 4, h, 4, 4);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        carte.setOpaque(false);
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel headerCarte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerCarte.setOpaque(false);
        headerCarte.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerCarte.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        Theme t = GestionnaireTheme.actif();
        JLabel lblIcone = new JLabel(IconeLucide.de(nomIcone, 16, couleur));
        headerCarte.add(lblIcone);

        JLabel lblTitreCarte = new JLabel(titre);
        lblTitreCarte.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lblTitreCarte.setForeground(couleur.get());
        headerCarte.add(lblTitreCarte);

        carte.add(headerCarte);
        carte.add(Box.createVerticalStrut(8));

        return carte;
    }

    private void preRemplir(Marchandise m) {
        txtNumeroConteneur.setText(m.getNumeroConteneur());
        txtCargaison.setText(m.getDescription());
        txtPoids.setText(String.valueOf((int) m.getPoids()));
        txtOrigine.setText(m.getOrigine());
        txtDestination.setText(m.getDestination());
        txtDateArrivee.setText(m.getDateArrivee() != null ? m.getDateArrivee().format(DATE_FMT) : "");
        txtCompagnie.setText(m.getCompagnieMaritime());
        cmbStatut.setSelectedItem(m.getStatut());

        if (m instanceof ConteneurRefrigere cr) {
            cmbType.setSelectedItem("Refrigere");
            txtTempMin.setText(String.format(java.util.Locale.US, "%.1f", cr.getTemperatureMin()));
            txtTempMax.setText(String.format(java.util.Locale.US, "%.1f", cr.getTemperatureMax()));
        } else if (m instanceof ConteneurStandard cs) {
            cmbType.setSelectedItem("Standard");
            txtContenu.setText(cs.getContenu());
        }

        cmbType.setEnabled(false);
    }

    private boolean valider() {
        lblErreur.setText(" ");

        if (txtNumeroConteneur.getText().trim().isEmpty()) {
            lblErreur.setText("Le N\u00B0 conteneur est obligatoire.");
            return false;
        }
        // Le numero est genere automatiquement, on le normalise si vide accidentel
        if (txtCargaison.getText().trim().isEmpty()) {
            lblErreur.setText("Le nom de la cargaison est obligatoire.");
            return false;
        }
        if (txtOrigine.getText().trim().isEmpty()) {
            lblErreur.setText("L'origine est obligatoire.");
            return false;
        }
        if (txtDestination.getText().trim().isEmpty()) {
            lblErreur.setText("La destination est obligatoire.");
            return false;
        }
        if (txtCompagnie.getText().trim().isEmpty()) {
            lblErreur.setText("La compagnie maritime est obligatoire.");
            return false;
        }

        double poids;
        try {
            poids = Double.parseDouble(txtPoids.getText().trim());
            if (poids <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            lblErreur.setText("Le poids doit etre un nombre positif.");
            return false;
        }

        try {
            LocalDate.parse(txtDateArrivee.getText().trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            lblErreur.setText("La date doit etre au format jj/MM/aaaa.");
            return false;
        }

        if ("Refrigere".equals(cmbType.getSelectedItem())) {
            double tempMin, tempMax;
            try {
                tempMin = Double.parseDouble(txtTempMin.getText().trim());
                tempMax = Double.parseDouble(txtTempMax.getText().trim());
            } catch (NumberFormatException e) {
                lblErreur.setText("Les temperatures doivent etre des nombres valides.");
                return false;
            }
            if (tempMin >= tempMax) {
                lblErreur.setText("La temperature min doit etre inferieure a la temperature max.");
                return false;
            }
        }

        if ("Standard".equals(cmbType.getSelectedItem()) && txtContenu.getText().trim().isEmpty()) {
            lblErreur.setText("Le contenu du conteneur standard est obligatoire.");
            return false;
        }

        return true;
    }

    private static String genererProchainNumeroConteneur(java.util.List<Integer> idsExistants) {
        int maxId = 0;
        if (idsExistants != null) {
            for (Integer id : idsExistants) {
                if (id != null && id > maxId) maxId = id;
            }
        }
        return "CONT-" + String.format("%04d", maxId + 1);
    }

    private Marchandise construireMarchandise() {
        int id;
        if (mode == Mode.MODIFIER && existant != null) {
            id = existant.getId();
        } else {
            id = idsExistants != null && !idsExistants.isEmpty()
                ? idsExistants.stream().mapToInt(Integer::intValue).max().orElse(0) + 1
                : 1;
        }

        String description = txtCargaison.getText().trim();
        double poids = Double.parseDouble(txtPoids.getText().trim());
        String origine = txtOrigine.getText().trim();
        String destination = txtDestination.getText().trim();
        LocalDate dateArrivee = LocalDate.parse(txtDateArrivee.getText().trim(), DATE_FMT);
        String compagnie = txtCompagnie.getText().trim();
        String statut = (String) cmbStatut.getSelectedItem();
        String numero = txtNumeroConteneur.getText().trim();

        Marchandise m;
        if ("Refrigere".equals(cmbType.getSelectedItem())) {
            double tempMin = Double.parseDouble(txtTempMin.getText().trim());
            double tempMax = Double.parseDouble(txtTempMax.getText().trim());
            m = new ConteneurRefrigere(id, poids, description, tempMin, tempMax);
        } else {
            String contenu = txtContenu.getText().trim();
            m = new ConteneurStandard(id, poids, description, contenu);
        }

        m.setNumeroConteneur(numero);
        m.setOrigine(origine);
        m.setDestination(destination);
        m.setDateArrivee(dateArrivee);
        m.setCompagnieMaritime(compagnie);
        m.setStatut(statut);

        return m;
    }

    public Marchandise afficher() {
        setVisible(true);
        return resultat;
    }
}
