package metier.ui;

import metier.*;
import metier.persistance.GestionDonnees;
import metier.threads.MoniteurTemperature;
import metier.ui.dialogs.CommandePalette;
import metier.ui.dialogs.DialogueCargaison;
import metier.ui.dialogs.ServiceToast;
import metier.ui.panels.*;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fenetre principale de l'application de gestion du fret portuaire.
 * Design Linear/Vercel : sidebar 68px, header 60px, contenu central,
 * slide-in detail, Ctrl+K palette, toasts bottom-right.
 */
public class FenetrePrincipale extends JFrame {

    private final GestionDonnees gestionDonnees;
    private final CardLayout cardLayout;
    private final JPanel mainContent;

    private BarreLaterale barreLaterale;
    private BarreEntete barreEntete;
    private PanneauTableauDeBord tableauDeBord;
    private PanneauListeCargaisons listeCargaisons;
    private PanneauDetailGlissant panneauDetail;

    private JPanel glass;
    private JPanel scrim;
    private CommandePalette palette;

    private MoniteurTemperature moniteurTemperature;
    private Thread threadTemperature;
    private KeyEventDispatcher dispatcherClavier;

    private JLabel lblStatusDroit;

    private static final DateTimeFormatter HEURE_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FenetrePrincipale() {
        super("Gestion du Fret \u2014 Port Autonome de Douala");

        gestionDonnees = new GestionDonnees();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1320, 820);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // ==== Sidebar 68px a gauche ====
        barreLaterale = new BarreLaterale(this::naviguerVers);
        add(barreLaterale, BorderLayout.WEST);

        // ==== Zone droite : header + centre + status ====
        JPanel droite = new JPanel(new BorderLayout());
        Theme t = GestionnaireTheme.actif();
        droite.setBackground(t.bg());

        // Header
        barreEntete = new BarreEntete();
        barreEntete.setTitre("Vue d'ensemble", "Port Autonome de Douala");
        droite.add(barreEntete, BorderLayout.NORTH);

        // Contenu central : CardLayout simple dans un BorderLayout.CENTER
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(t.bg());

        tableauDeBord = new PanneauTableauDeBord();
        listeCargaisons = new PanneauListeCargaisons(gestionDonnees);

        mainContent.add(tableauDeBord, BarreLaterale.VUE_TABLEAU_BORD);
        mainContent.add(listeCargaisons, BarreLaterale.VUE_CARGAISONS);

        droite.add(mainContent, BorderLayout.CENTER);

        // Status bar en bas
        droite.add(creerBarreStatut(), BorderLayout.SOUTH);

        add(droite, BorderLayout.CENTER);

        // ==== Glass pane : scrim + slide-in detail ====
        glass = new JPanel(null) {
            @Override
            public boolean contains(int x, int y) {
                // Only consume events when visible
                if (!isVisible()) return false;
                // Check if the point is within the detail panel
                if (panneauDetail != null && panneauDetail.isVisible()) {
                    Point detailLoc = panneauDetail.getLocation();
                    if (x >= detailLoc.x && x < detailLoc.x + panneauDetail.getWidth()
                        && y >= detailLoc.y && y < detailLoc.y + panneauDetail.getHeight()) {
                        return true;
                    }
                }
                // Scrim area
                return panneauDetail != null && panneauDetail.estVisibleLogique();
            }
        };
        glass.setOpaque(false);
        glass.setVisible(false);

        scrim = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (panneauDetail != null && panneauDetail.estVisibleLogique()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        scrim.setOpaque(false);
        scrim.setBounds(0, 0, 1000, 600);
        scrim.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Only close if the click is on the scrim, not on the detail panel
                if (panneauDetail != null && panneauDetail.estVisibleLogique()) {
                    Point detailLoc = panneauDetail.getLocation();
                    int clickX = e.getX();
                    // If click is to the left of the detail panel, it's on the scrim
                    if (clickX < detailLoc.x || clickX >= detailLoc.x + panneauDetail.getWidth()) {
                        panneauDetail.masquer(glass.getWidth());
                    }
                }
            }
        });
        glass.add(scrim);

        panneauDetail = new PanneauDetailGlissant();
        panneauDetail.setBounds(1000, 0, PanneauDetailGlissant.LARGEUR, 600);
        glass.add(panneauDetail);

        // Move detail panel to the front (on top of scrim) so it gets clicks first
        glass.setComponentZOrder(panneauDetail, 0);
        glass.setComponentZOrder(scrim, 1);

        setGlassPane(glass);

        // Resize du glass pane
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getContentPane().getWidth();
                int h = getContentPane().getHeight();
                glass.setBounds(0, 0, w, h);
                scrim.setBounds(0, 0, w, h);
                int x = panneauDetail.estVisibleLogique()
                    ? Math.min(w - PanneauDetailGlissant.LARGEUR, w)
                    : w;
                panneauDetail.setBounds(x, 0, PanneauDetailGlissant.LARGEUR, h);
                panneauDetail.setSize(PanneauDetailGlissant.LARGEUR, h);
            }
        });

        // Listeners
        listeCargaisons.setOnVoirDetail(this::ouvrirDetail);
        listeCargaisons.setOnAjouter(this::ajouterCargaison);
        listeCargaisons.setOnModifier(this::modifierCargaison);
        listeCargaisons.setOnSupprimer(this::supprimerCargaison);
        listeCargaisons.setOnSauvegarder(this::sauvegarder);
        listeCargaisons.setOnCharger(this::charger);
        listeCargaisons.setOnRecherche(r -> { /* branche sur champ header si besoin */ });
        panneauDetail.setOnModifier(this::modifierCargaisonExistante);
        panneauDetail.setOnSupprimer(this::supprimerCargaisonExistante);
        panneauDetail.setOnFermetureComplete(() -> glass.setVisible(false));

        barreEntete.setOnRecherche(texte -> listeCargaisons.appliquerRecherche(texte));
        // Theme deja gere par la navigation "THEME" via bouton label
        // barreLaterale.setSurActionTheme(this::basculerTheme);

        // Theme listener -> repeindre
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            getContentPane().setBackground(tt.bg());
            droite.setBackground(tt.bg());
            mainContent.setBackground(tt.bg());
            if (scrim != null) scrim.repaint();
        });

        // Dispatcher global pour Ctrl+K
        installerRaccourcisClavier();

        chargerDonnees();
        demarrerSurveillance();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                fermerApplication();
            }
        });

        actualiserAffichage();
        cardLayout.show(mainContent, BarreLaterale.VUE_TABLEAU_BORD);
    }

    // ==================== BARRE STATUT ====================

    private JPanel creerBarreStatut() {
        Theme t = GestionnaireTheme.actif();
        JPanel barreStatut = new JPanel(new BorderLayout());
        barreStatut.setBackground(t.surface());
        barreStatut.setPreferredSize(new Dimension(0, 28));
        barreStatut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, t.bordure()),
            new EmptyBorder(0, 18, 0, 18)
        ));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gauche.setOpaque(false);
        JLabel point = new JLabel("\u25CF");
        point.setForeground(t.succes());
        point.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        gauche.add(point);
        JLabel lbl = new JLabel("Systeme actif  \u2022  Surveillance en cours");
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lbl.setForeground(t.texteSecondaire());
        gauche.add(lbl);
        barreStatut.add(gauche, BorderLayout.WEST);

        lblStatusDroit = new JLabel("");
        lblStatusDroit.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblStatusDroit.setForeground(t.texteAtténué());
        barreStatut.add(lblStatusDroit, BorderLayout.EAST);

        // Theme change listener for status bar
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            barreStatut.setBackground(tt.surface());
            barreStatut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, tt.bordure()),
                new EmptyBorder(0, 18, 0, 18)
            ));
            point.setForeground(tt.succes());
            lbl.setForeground(tt.texteSecondaire());
            lblStatusDroit.setForeground(tt.texteAtténué());
            barreStatut.repaint();
        });

        return barreStatut;
    }

    // ==================== RACCOURCIS CLAVIER ====================

    private void installerRaccourcisClavier() {
        dispatcherClavier = e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            boolean ctrl = e.isControlDown();
            boolean shift = e.isShiftDown();
            if (ctrl && e.getKeyCode() == KeyEvent.VK_K) {
                ouvrirPalette();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_N) {
                ajouterCargaison();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_E) {
                modifierCargaison();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_D) {
                supprimerCargaison();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_S) {
                sauvegarder();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_O) {
                charger();
                e.consume();
                return true;
            }
            if (ctrl && shift && e.getKeyCode() == KeyEvent.VK_T) {
                basculerTheme();
                e.consume();
                return true;
            }
            if (ctrl && e.getKeyCode() == KeyEvent.VK_Q) {
                fermerApplication();
                e.consume();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (panneauDetail != null && panneauDetail.estVisibleLogique()) {
                    panneauDetail.masquer(getContentPane().getWidth());
                    return true;
                }
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(dispatcherClavier);
    }

    private void ouvrirPalette() {
        if (palette != null && palette.isVisible()) {
            palette.fermer();
            return;
        }
        if (palette != null) {
            palette.dispose();
        }
        palette = new CommandePalette(this, construireCommandes(), null);
        palette.afficher();
    }

    private List<CommandePalette.Commande> construireCommandes() {
        List<CommandePalette.Commande> cmds = new ArrayList<>();
        cmds.add(new CommandePalette.Commande("Tableau de bord", "Revenir a la vue d'ensemble",
            "layout-dashboard", () -> naviguerVers(BarreLaterale.VUE_TABLEAU_BORD), ""));
        cmds.add(new CommandePalette.Commande("Cargaisons", "Voir toutes les cargaisons",
            "package", () -> naviguerVers(BarreLaterale.VUE_CARGAISONS), ""));
        cmds.add(new CommandePalette.Commande("Ajouter une cargaison", "Creer une nouvelle entree",
            "plus", this::ajouterCargaison, "Ctrl+N"));
        cmds.add(new CommandePalette.Commande("Modifier la selection", "Editer la cargaison choisie",
            "pencil", this::modifierCargaison, "Ctrl+E"));
        cmds.add(new CommandePalette.Commande("Supprimer la selection", "Retirer la cargaison choisie",
            "trash-2", this::supprimerCargaison, "Ctrl+D"));
        cmds.add(new CommandePalette.Commande("Sauvegarder", "Ecrire port_douala.ser",
            "save", this::sauvegarder, "Ctrl+S"));
        cmds.add(new CommandePalette.Commande("Charger", "Lire port_douala.ser",
            "upload", this::charger, "Ctrl+O"));
        cmds.add(new CommandePalette.Commande("Basculer le theme", "Sombre / clair",
            GestionnaireTheme.estSombre() ? "sun" : "moon", this::basculerTheme, "Ctrl+Shift+T"));
        cmds.add(new CommandePalette.Commande("Quitter", "Fermer l'application",
            "x", this::fermerApplication, "Ctrl+Q"));
        return cmds;
    }

    private void basculerTheme() {
        GestionnaireTheme.definirMode(!GestionnaireTheme.estSombre());
        // Mettre a jour le titre selon la vue
        mettreAJourTitre();
    }

    private void mettreAJourTitre() {
        // (Hook pour adapter le sous-titre ; on garde simple)
    }

    // ==================== DONNEES ====================

    private void chargerDonnees() {
        try {
            if (gestionDonnees.fichierExiste()) {
                gestionDonnees.charger();
                ServiceToast.succes("Donnees chargees depuis port_douala.ser");
            } else {
                gestionDonnees.initialiserDonneesDemo();
                ServiceToast.info("Bienvenue ! Donnees de demonstration chargees.");
            }
        } catch (Exception e) {
            gestionDonnees.initialiserDonneesDemo();
            ServiceToast.avertir("Erreur de chargement. Donnees demo utilisees.");
        }
    }

    private void demarrerSurveillance() {
        moniteurTemperature = new MoniteurTemperature(gestionDonnees, this::actualiserAffichage);
        threadTemperature = new Thread(moniteurTemperature, "Moniteur-Temperature");
        threadTemperature.setDaemon(true);
        threadTemperature.start();
    }

    // ==================== NAVIGATION ====================

    private void naviguerVers(String vue) {
        switch (vue) {
            case BarreLaterale.VUE_TABLEAU_BORD:
                cardLayout.show(mainContent, BarreLaterale.VUE_TABLEAU_BORD);
                barreEntete.setTitre("Vue d'ensemble", "Port Autonome de Douala");
                break;
            case BarreLaterale.VUE_CARGAISONS:
                cardLayout.show(mainContent, BarreLaterale.VUE_CARGAISONS);
                barreEntete.setTitre("Cargaisons", "Liste et gestion du fret");
                break;
            case "SAUVEGARDER":
                sauvegarder();
                break;
            case "CHARGER":
                charger();
                break;
            case "THEME":
                basculerTheme();
                break;
        }
        if (panneauDetail != null && panneauDetail.estVisibleLogique()) {
            panneauDetail.masquer(getContentPane().getWidth());
            glass.setVisible(false);
        }
        actualiserAffichage();
    }

    private void ouvrirDetail(Marchandise m) {
        // Aller sur la vue cargaisons d'abord
        cardLayout.show(mainContent, BarreLaterale.VUE_CARGAISONS);
        barreLaterale.setConteneurActif(BarreLaterale.VUE_CARGAISONS);
        barreEntete.setTitre("Cargaisons", m.getNumeroConteneur());
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        glass.setBounds(0, 0, w, h);
        scrim.setBounds(0, 0, w, h);
        panneauDetail.setBounds(w, 0, PanneauDetailGlissant.LARGEUR, h);
        glass.setVisible(true);
        scrim.setVisible(true);
        panneauDetail.afficher(m, w);
    }

    // ==================== OPERATIONS CRUD ====================

    private void ajouterCargaison() {
        naviguerVers(BarreLaterale.VUE_CARGAISONS);
        List<Marchandise> toutes = gestionDonnees.getToutes();
        java.util.List<Integer> ids = toutes.stream().map(Marchandise::getId).toList();

        DialogueCargaison dialogue = new DialogueCargaison(
            this, "Ajouter une Cargaison", DialogueCargaison.Mode.AJOUTER, null, ids);
        Marchandise nouvelle = dialogue.afficher();

        if (nouvelle != null) {
            gestionDonnees.ajouter(nouvelle);
            actualiserAffichage();
            ServiceToast.succes("Cargaison " + nouvelle.getNumeroConteneur() + " ajoutee.");
        }
    }

    private void modifierCargaison() {
        Marchandise selectionnee = listeCargaisons.getCargaisonSelectionnee();
        if (selectionnee == null) {
            ServiceToast.avertir("Veuillez selectionner une cargaison a modifier.");
            return;
        }
        modifierCargaisonExistante(selectionnee);
    }

    private void modifierCargaisonExistante(Marchandise existante) {
        List<Marchandise> toutes = gestionDonnees.getToutes();
        java.util.List<Integer> ids = toutes.stream().map(Marchandise::getId).toList();

        DialogueCargaison dialogue = new DialogueCargaison(
            this, "Modifier la Cargaison", DialogueCargaison.Mode.MODIFIER, existante, ids);
        Marchandise modifiee = dialogue.afficher();

        if (modifiee != null) {
            gestionDonnees.modifier(modifiee);
            actualiserAffichage();
            panneauDetail.afficher(modifiee, getContentPane().getWidth());
            ServiceToast.succes("Cargaison " + modifiee.getNumeroConteneur() + " modifiee.");
        }
    }

    private void supprimerCargaison() {
        Marchandise selectionnee = listeCargaisons.getCargaisonSelectionnee();
        if (selectionnee == null) {
            ServiceToast.avertir("Veuillez selectionner une cargaison a supprimer.");
            return;
        }
        supprimerCargaisonExistante(selectionnee);
    }

    private void supprimerCargaisonExistante(Marchandise selectionnee) {
        int response = JOptionPane.showConfirmDialog(this,
            "Etes-vous sur de vouloir supprimer la cargaison " + selectionnee.getNumeroConteneur() + " ?\n" +
            "Cette action est irreversible.",
            "Confirmer la suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            gestionDonnees.supprimer(selectionnee.getId());
            actualiserAffichage();
            if (panneauDetail != null && panneauDetail.estVisibleLogique()) {
                panneauDetail.masquer(getContentPane().getWidth());
            }
            ServiceToast.info("Cargaison " + selectionnee.getNumeroConteneur() + " supprimee.");
        }
    }

    // ==================== PERSISTANCE ====================

    private void sauvegarder() {
        try {
            gestionDonnees.sauvegarder();
            ServiceToast.succes("Donnees sauvegardees dans port_douala.ser");
        } catch (IOException e) {
            ServiceToast.erreur("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    private void charger() {
        try {
            if (!gestionDonnees.fichierExiste()) {
                ServiceToast.avertir("Aucun fichier de sauvegarde trouve.");
                return;
            }
            gestionDonnees.charger();
            actualiserAffichage();
            cardLayout.show(mainContent, BarreLaterale.VUE_TABLEAU_BORD);
            ServiceToast.succes("Donnees chargees avec succes.");
        } catch (Exception e) {
            ServiceToast.erreur("Erreur de chargement : " + e.getMessage());
        }
    }

    // ==================== ACTUALISATION ====================

    public void actualiserAffichage() {
        Map<String, Object> stats = gestionDonnees.getStatistiques();
        tableauDeBord.actualiser(stats);
        listeCargaisons.actualiser();

        // Mettre a jour le graphique dans le dashboard
        List<ConteneurRefrigere> refrigeres = gestionDonnees.getRefrigeres();
        tableauDeBord.mettreAJourGraphique(refrigeres,
            gestionDonnees.snapshotHistoriques());

        // Compteur en bas
        int total = gestionDonnees.getToutes().size();
        int alertes = gestionDonnees.getToutes().stream()
            .filter(m -> m instanceof ConteneurRefrigere cr && cr.estEnAlerte())
            .mapToInt(m -> 1).sum();
        StringBuilder sb = new StringBuilder();
        sb.append(total).append(" cargaison").append(total > 1 ? "s" : "");
        if (alertes > 0) {
            sb.append("  \u2022  ").append(alertes).append(" alerte").append(alertes > 1 ? "s" : "");
        }
        sb.append("  \u2022  Mise a jour ").append(LocalDateTime.now().format(HEURE_FMT));
        lblStatusDroit.setText(sb.toString());
    }

    // ==================== FERMETURE ====================

    private void fermerApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous sauvegarder avant de quitter ?",
            "Quitter l'application",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.CANCEL_OPTION) {
            return;
        }

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gestionDonnees.sauvegarder();
            } catch (IOException e) {
                int force = JOptionPane.showConfirmDialog(this,
                    "Erreur de sauvegarde : " + e.getMessage() + "\nQuitter sans sauvegarder ?",
                    "Erreur", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (force != JOptionPane.YES_OPTION) return;
            }
        }

        if (moniteurTemperature != null) {
            moniteurTemperature.arreter();
        }

        if (dispatcherClavier != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(dispatcherClavier);
        }

        dispose();
        System.exit(0);
    }
}
