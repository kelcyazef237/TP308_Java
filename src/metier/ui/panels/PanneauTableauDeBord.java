package metier.ui.panels;

import metier.ConteneurRefrigere;
import metier.persistance.MesureTemperature;
import metier.ui.components.CarteMetrique;
import metier.ui.components.GraphiqueTemperature;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Tableau de bord : 8 cartes de metriques en grille 4x2 + graphique des temperatures.
 * Style compact (Linear/Vercel) : icone + valeur + label.
 */
public class PanneauTableauDeBord extends JPanel {

    private final CarteMetrique carteTotal;
    private final CarteMetrique carteStandard;
    private final CarteMetrique carteRefrigere;
    private final CarteMetrique carteAlertes;
    private final CarteMetrique carteTaxeTotale;
    private final CarteMetrique carteArrivees;
    private final CarteMetrique carteTempMoy;
    private final CarteMetrique carteSurcharge;
    private final GraphiqueTemperature graphique;

    public PanneauTableauDeBord() {
        Theme t = GestionnaireTheme.actif();
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 28, 20, 28));

        // En-tete compact
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel titre = new JLabel("Vue d'ensemble");
        titre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titre.setForeground(t.textePrimaire());
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sousTitre = new JLabel("Suivi en temps reel du trafic de fret");
        sousTitre.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sousTitre.setForeground(t.texteSecondaire());
        sousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        sousTitre.setBorder(new EmptyBorder(2, 0, 0, 0));

        header.add(titre);
        header.add(sousTitre);
        add(header, BorderLayout.NORTH);

        // Theme change listener for dashboard header
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            titre.setForeground(tt.textePrimaire());
            sousTitre.setForeground(tt.texteSecondaire());
            repaint();
        });

        // Centre : grille 4x2 en haut + graphique en bas
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        // Grille 4x2 — wrap dans un panneau de remplissage
        JPanel grilleWrap = new JPanel(new GridLayout(2, 4, 12, 12));
        grilleWrap.setOpaque(false);

        carteTotal = new CarteMetrique("Total cargaisons", "package",
            () -> GestionnaireTheme.actif().accent());
        carteStandard = new CarteMetrique("Standard", "ship",
            () -> GestionnaireTheme.actif().info());
        carteRefrigere = new CarteMetrique("Refrigerees", "snowflake",
            () -> new Color(0x56, 0xC2, 0xE0));
        carteAlertes = new CarteMetrique("En alerte", "triangle-alert",
            () -> GestionnaireTheme.actif().danger());
        carteTaxeTotale = new CarteMetrique("Taxe totale", "anchor",
            () -> GestionnaireTheme.actif().succes());
        carteArrivees = new CarteMetrique("Arrivees aujourd'hui", "check",
            () -> GestionnaireTheme.actif().succes());
        carteTempMoy = new CarteMetrique("Temperature moyenne", "snowflake",
            () -> new Color(0x56, 0xC2, 0xE0));
        carteSurcharge = new CarteMetrique("Surcharge energetique", "settings",
            () -> GestionnaireTheme.actif().avertissement());

        grilleWrap.add(carteTotal);
        grilleWrap.add(carteStandard);
        grilleWrap.add(carteRefrigere);
        grilleWrap.add(carteAlertes);
        grilleWrap.add(carteTaxeTotale);
        grilleWrap.add(carteArrivees);
        grilleWrap.add(carteTempMoy);
        grilleWrap.add(carteSurcharge);

        grilleWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        grilleWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        grilleWrap.setPreferredSize(new Dimension(0, 220));
        centre.add(grilleWrap);
        centre.add(Box.createVerticalStrut(16));

        // Graphique
        graphique = new GraphiqueTemperature();
        graphique.setAlignmentX(Component.LEFT_ALIGNMENT);
        graphique.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        graphique.setPreferredSize(new Dimension(0, 320));
        centre.add(graphique);

        JPanel centreWrap = new JPanel(new BorderLayout());
        centreWrap.setOpaque(false);
        centreWrap.add(centre, BorderLayout.NORTH);
        add(centreWrap, BorderLayout.CENTER);
    }

    public void actualiser(java.util.Map<String, Object> stats) {
        if (stats == null) return;

        Object total = stats.get("total");
        if (total != null) carteTotal.setValeur(String.valueOf(total));

        Object standard = stats.get("standard");
        if (standard != null) carteStandard.setValeur(String.valueOf(standard));

        Object refrigere = stats.get("refrigere");
        if (refrigere != null) carteRefrigere.setValeur(String.valueOf(refrigere));

        Object alertes = stats.get("alertes");
        if (alertes != null) {
            carteAlertes.setValeur(String.valueOf(alertes));
            long n = ((Number) alertes).longValue();
            if (n > 0) carteAlertes.setCouleurValeur(GestionnaireTheme.actif().danger());
            else carteAlertes.setCouleurValeur(GestionnaireTheme.actif().textePrimaire());
        }

        Object taxeTotale = stats.get("taxeTotale");
        if (taxeTotale != null) carteTaxeTotale.setValeur(formaterCFA(((Number) taxeTotale).doubleValue()));

        Object arrivees = stats.get("arriveesAujourdhui");
        if (arrivees != null) carteArrivees.setValeur(String.valueOf(arrivees));

        Object tempMoy = stats.get("temperatureMoyenne");
        if (tempMoy != null) {
            double tt = ((Number) tempMoy).doubleValue();
            carteTempMoy.setValeur(tt > 0 ? String.format("%.1f \u00B0C", tt) : "-- \u00B0C");
        }

        Object surcharge = stats.get("surchargeEnergetique");
        if (surcharge != null) carteSurcharge.setValeur(formaterCFA(((Number) surcharge).doubleValue()));
    }

    public void mettreAJourGraphique(List<ConteneurRefrigere> refrigeres,
                                     Map<Integer, Deque<MesureTemperature>> historique) {
        graphique.setConteneurs(refrigeres);
        graphique.actualiser(historique);
    }

    private String formaterCFA(double valeur) {
        if (valeur >= 1_000_000) return String.format("%.1fM", valeur / 1_000_000);
        if (valeur >= 1_000) return String.format("%.1fk", valeur / 1_000);
        return String.format("%.0f", valeur);
    }
}
