package fr.tp308.threading;

import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.service.IConteneurService;
import fr.tp308.ihm.ui.MarchandiseTableModel;
import fr.tp308.ihm.ui.StatusBar;

import javax.swing.*;
import java.util.Date;
import java.util.List;

/**
 * Chef d'orchestre du multithreading.
 * ⚡ S'intègre dans MainFrame de l'équipe IHM.
 */
public class GestionnaireThreads {

    private SimulateurTemperature simulateur;
    private BarreProgressionWorker barreWorker;

    // Composants IHM réels (fournis par MainFrame)
    private final IConteneurService    conteneurService;
    private final MarchandiseTableModel tableModel;
    private final StatusBar            statusBar;
    private final JTextArea            journalAlertes; // peut être null

    public GestionnaireThreads(IConteneurService conteneurService,
                               MarchandiseTableModel tableModel,
                               StatusBar statusBar,
                               JTextArea journalAlertes) {
        this.conteneurService = conteneurService;
        this.tableModel       = tableModel;
        this.statusBar        = statusBar;
        this.journalAlertes   = journalAlertes;
    }

    // ─── Contrôle ─────────────────────────────────────────────────────

    public void demarrerSimulation() {
        if (simulateur != null && simulateur.estActif()) return;
        simulateur = new SimulateurTemperature(
                conteneurService,
                this::onMiseAJour,
                this::onAlerte
        );
        simulateur.demarrer();
    }

    public void arreterSimulation() {
        if (simulateur != null) simulateur.arreter();
        if (barreWorker != null) barreWorker.cancel(true);
    }

    public void pauseSimulation() {
        if (simulateur != null) simulateur.mettreEnPause();
    }

    public void reprendreSimulation() {
        if (simulateur != null) simulateur.reprendre();
    }

    public boolean estActif()   {
        return simulateur != null && simulateur.estActif();
    }

    public boolean estEnPause() {
        return simulateur != null && simulateur.estEnPause();
    }

    // ─── Callbacks ────────────────────────────────────────────────────

    /**
     * Appelé après chaque cycle — déjà sur l'EDT.
     * Met à jour la JTable via le vrai MarchandiseTableModel.
     */
    private void onMiseAJour(List<ConteneurDto> liste) {
        // Mise à jour du tableau
        tableModel.setData(liste);

        // Mise à jour de la barre de statut
        long nbAlertes = liste.stream()
                .filter(ConteneurDto::isAlerte)
                .count();

        if (statusBar != null) {
            statusBar.showProgress(
                    String.format("Simulation active | %d conteneurs | ⚠ %d alertes",
                            liste.size(), nbAlertes),
                    liste.size()
            );
        }

        // Lancer la barre de progression pour le prochain cycle
        lancerBarreProgression();
    }

    /**
     * Appelé quand un conteneur change d'état d'alerte — déjà sur l'EDT.
     */
    private void onAlerte(ConteneurDto dto) {
        String msg = String.format("[%tT] ALERTE %s : %.1f°C%n",
                new Date(), dto.getCode(), dto.getTemperature());

        if (journalAlertes != null) {
            journalAlertes.append(msg);
            journalAlertes.setCaretPosition(
                    journalAlertes.getDocument().getLength());
        }

        java.awt.Toolkit.getDefaultToolkit().beep();
        LOG.info(msg);
    }

    private void lancerBarreProgression() {
        if (barreWorker != null && !barreWorker.isDone()) {
            barreWorker.cancel(true);
        }
        // Barre de progression sans composant Swing externe pour l'instant
        // Elle sera connectée quand l'équipe IHM expose sa JProgressBar
    }

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(GestionnaireThreads.class.getName());
}