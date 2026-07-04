package fr.tp308.threading;

import metier.ConteneurRefrigere;
import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.service.IConteneurService;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Simule la variation de température de tous les ConteneurRefrigere
 * toutes les 3 secondes et notifie l'IHM via callback.
 *
 * ⚡ Utilise le VRAI ConteneurRefrigere de l'équipe Core :
 *    → mettreAJourTemperature(double) retourne boolean
 *    → estEnAlerte() pour vérifier l'état
 */
public class SimulateurTemperature extends SimulateurBase {

    public static final long INTERVALLE_MS = 3_000L;

    // Paramètres physiques de simulation
    private static final double VARIATION_MAX      = 0.8;
    private static final double DERIVE_AMBIANTE    = 0.10;
    private static final double CORRECTION_SYSTEME = 0.15;

    private final Random random = new Random();
    private final StatistiquesSimulation stats = StatistiquesSimulation.getInstance();

    // ⚡ Service IHM → pour récupérer la liste des conteneurs réfrigérés
    private final IConteneurService conteneurService;

    // Callback → notifie l'IHM après chaque cycle
    private final Consumer<List<ConteneurDto>> callbackMiseAJour;

    // Callback → notifie en cas de changement d'état d'alerte
    private final Consumer<ConteneurDto> callbackAlerte;

    public SimulateurTemperature(
            IConteneurService conteneurService,
            Consumer<List<ConteneurDto>> callbackMiseAJour,
            Consumer<ConteneurDto> callbackAlerte) {
        super("Thread-Simulation-Temperature");
        this.conteneurService  = conteneurService;
        this.callbackMiseAJour = callbackMiseAJour;
        this.callbackAlerte    = callbackAlerte;
    }

    @Override
    protected void initialiser() {
        stats.reset();
        stats.demarrerChrono();
        LOG.info("Simulateur température démarré — intervalle: " + INTERVALLE_MS + "ms");
    }

    @Override
    protected void executerCycle() throws InterruptedException {
        // 1. Récupérer tous les conteneurs réfrigérés via le service IHM
        List<ConteneurDto> tousLesConteneurs = conteneurService.findAll();

        // Filtrer uniquement les réfrigérés (type = "Réfrigéré")
        List<ConteneurDto> refrigeres = tousLesConteneurs.stream()
                .filter(dto -> "Réfrigéré".equalsIgnoreCase(dto.getType()))
                .toList();

        stats.setConteneursActifs(refrigeres.size());
        stats.incrementerCycles();

        // 2. Simuler température pour chaque réfrigéré
        for (ConteneurDto dto : refrigeres) {
            simulerUnConteneur(dto);
        }

        // 3. Notifier l'IHM sur l'EDT (règle d'or Swing)
        final List<ConteneurDto> snapshot = List.copyOf(tousLesConteneurs);
        SwingUtilities.invokeLater(() -> {
            if (callbackMiseAJour != null) {
                callbackMiseAJour.accept(snapshot);
            }
        });

        LOG.fine("Cycle " + stats.getCyclesTotal() + " — "
                + refrigeres.size() + " réfrigérés traités.");
    }

    private void simulerUnConteneur(ConteneurDto dto) {
        if (dto.getTemperature() == null) return;

        double tempActuelle = dto.getTemperature();
        double tempMin      = dto.getTempMin() != null ? dto.getTempMin() : -25.0;
        double tempMax      = dto.getTempMax() != null ? dto.getTempMax() : 5.0;
        double consigne     = (tempMin + tempMax) / 2.0;

        // Variation aléatoire (bruit thermique)
        double variation = (random.nextDouble() * 2 - 1) * VARIATION_MAX;

        // Dérive ambiante (chaleur de Douala)
        double derive = DERIVE_AMBIANTE * Math.signum(28.0 - tempActuelle);

        // Correction système de froid
        double correction = 0;
        if (tempActuelle > consigne + 0.5)      correction = -CORRECTION_SYSTEME;
        else if (tempActuelle < consigne - 0.5) correction = +CORRECTION_SYSTEME * 0.3;

        // Nouvelle température bornée physiquement
        double nouvelleTemp = tempActuelle + variation + derive + correction;
        nouvelleTemp = Math.max(tempMin - 10, Math.min(tempMax + 10, nouvelleTemp));

        // Mettre à jour le DTO directement
        boolean alerteChangee = dto.mettreAJourTemperature(nouvelleTemp);

        // Si l'état d'alerte a changé → notifier
        if (alerteChangee) {
            stats.incrementerAlertes();
            final ConteneurDto dtoAlerte = dto;
            SwingUtilities.invokeLater(() -> {
                if (callbackAlerte != null) callbackAlerte.accept(dtoAlerte);
            });
        }
    }

    @Override
    protected long getIntervalleMs() { return INTERVALLE_MS; }

    @Override
    protected void nettoyer() {
        LOG.info("Simulation terminée. Stats: " + stats);
    }
}