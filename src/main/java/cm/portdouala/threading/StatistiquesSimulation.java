package cm.portdouala.threading;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compteurs de performance de la simulation.
 * Toutes les opérations sont atomiques → utilisable depuis n'importe quel thread.
 *
 * ⚡ L'équipe IHM affichera ces stats dans la barre de statut.
 */
public class StatistiquesSimulation {

    private final AtomicInteger cyclesTotal        = new AtomicInteger(0);
    private final AtomicInteger alertesDetectees   = new AtomicInteger(0);
    private final AtomicInteger conteneursActifs   = new AtomicInteger(0);
    private final AtomicLong    tempsDebutMs       = new AtomicLong(0);

    // Singleton pour accès global
    private static final StatistiquesSimulation INSTANCE =
            new StatistiquesSimulation();

    private StatistiquesSimulation() {}
    public static StatistiquesSimulation getInstance() { return INSTANCE; }

    // ─── Mutateurs atomiques ──────────────────────────────────────────
    public void incrementerCycles()    { cyclesTotal.incrementAndGet(); }
    public void incrementerAlertes()   { alertesDetectees.incrementAndGet(); }
    public void setConteneursActifs(int n) { conteneursActifs.set(n); }
    public void demarrerChrono()       { tempsDebutMs.set(System.currentTimeMillis()); }
    public void reset() {
        cyclesTotal.set(0);
        alertesDetectees.set(0);
        conteneursActifs.set(0);
    }

    // ─── Accesseurs ───────────────────────────────────────────────────
    public int  getCyclesTotal()      { return cyclesTotal.get(); }
    public int  getAlertesDetectees() { return alertesDetectees.get(); }
    public int  getConteneursActifs() { return conteneursActifs.get(); }

    public String getDureeFormatee() {
        long debut = tempsDebutMs.get();
        if (debut == 0) return "00:00:00";
        long ecouleeMs = System.currentTimeMillis() - debut;
        long h  = ecouleeMs / 3_600_000;
        long m  = (ecouleeMs % 3_600_000) / 60_000;
        long s  = (ecouleeMs % 60_000) / 1_000;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    public String toString() {
        return String.format(
                "Cycles: %d | Alertes: %d | Conteneurs actifs: %d | Durée: %s",
                getCyclesTotal(), getAlertesDetectees(),
                getConteneursActifs(), getDureeFormatee());
    }
}