package cm.portdouala.threading;

import cm.portdouala.interfaces.ISimulateur;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Classe abstraite fondatrice de tous les simulateurs.
 *
 * Gère le cycle de vie commun :
 *   - Démarrage / Arrêt propre via flag volatile
 *   - Pause / Reprise via wait/notify
 *   - Logging centralisé
 *
 * Pattern Template Method : les sous-classes implémentent executerCycle()
 * qui est appelé à chaque itération de la boucle principale.
 */
public abstract class SimulateurBase implements ISimulateur, Runnable {

    protected static final Logger LOG =
            Logger.getLogger(SimulateurBase.class.getName());

    // ─── Flags de contrôle ────────────────────────────────────────────
    // volatile garantit la visibilité entre threads sans synchronisation lourde
    private volatile boolean actif  = false;
    private volatile boolean pause  = false;

    // AtomicBoolean pour les vérifications composées thread-safe
    private final AtomicBoolean demarrageDemande = new AtomicBoolean(false);

    // ─── Thread interne ───────────────────────────────────────────────
    private Thread threadInterne;
    private final String nomThread;
    private final int    priorite;

    // ─── Constructeur ─────────────────────────────────────────────────
    protected SimulateurBase(String nomThread, int priorite) {
        this.nomThread = nomThread;
        // Priorité entre Thread.MIN_PRIORITY (1) et Thread.MAX_PRIORITY (10)
        this.priorite  = Math.max(Thread.MIN_PRIORITY,
                Math.min(Thread.MAX_PRIORITY, priorite));
    }

    // ─── Implémentation ISimulateur ───────────────────────────────────

    @Override
    public synchronized void demarrer() {
        if (actif) {
            LOG.warning("Simulateur déjà actif : " + nomThread);
            return;
        }
        actif  = true;
        pause  = false;
        // Création d'un nouveau thread — daemon pour ne pas bloquer la JVM
        threadInterne = new Thread(this, nomThread);
        threadInterne.setPriority(priorite);
        threadInterne.setDaemon(true); // s'arrête avec la JVM
        threadInterne.start();
        LOG.info("▶ Simulateur démarré : " + nomThread);
    }

    @Override
    public void arreter() {
        actif = false;
        // Réveille le thread s'il est en pause (sinon il reste bloqué sur wait)
        synchronized (this) {
            pause = false;
            notifyAll();
        }
        // Attend la fin propre du thread (max 3 secondes)
        if (threadInterne != null && threadInterne.isAlive()) {
            try {
                threadInterne.join(3000);
                if (threadInterne.isAlive()) {
                    LOG.severe("⚠ Thread non terminé après 3s : " + nomThread);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("⏹ Simulateur arrêté : " + nomThread);
    }

    @Override
    public synchronized void mettreEnPause() {
        if (actif) {
            pause = true;
            LOG.info("⏸ Simulateur en pause : " + nomThread);
        }
    }

    @Override
    public synchronized void reprendre() {
        pause = false;
        notifyAll(); // réveille le thread bloqué sur wait()
        LOG.info("▶ Simulateur repris : " + nomThread);
    }

    @Override public boolean estActif()   { return actif; }
    @Override public boolean estEnPause() { return pause; }

    // ─── Boucle principale (Pattern Template Method) ──────────────────

    @Override
    public final void run() {
        try {
            initialiser(); // hook optionnel avant la boucle
            while (actif) {
                gererPause(); // bloque ici si en pause
                if (!actif) break; // vérification après reprise

                executerCycle(); // ← LA logique métier va ici

                attendreProchainCycle(); // sleep entre deux cycles
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("Thread interrompu proprement : " + nomThread);
        } finally {
            nettoyer(); // hook optionnel à la fin
            actif = false;
        }
    }

    // ─── Méthodes abstraites (contrat sous-classes) ───────────────────

    /** La logique métier d'un cycle (ex: simuler température) */
    protected abstract void executerCycle() throws InterruptedException;

    /** Durée en ms entre deux cycles */
    protected abstract long getIntervalleMs();

    // ─── Hooks optionnels ─────────────────────────────────────────────
    protected void initialiser() {}
    protected void nettoyer()    {}

    // ─── Utilitaires internes ─────────────────────────────────────────

    private synchronized void gererPause() throws InterruptedException {
        while (pause && actif) {
            wait(); // libère le moniteur et dort jusqu'à notifyAll()
        }
    }

    private void attendreProchainCycle() throws InterruptedException {
        long debut  = System.currentTimeMillis();
        long restant = getIntervalleMs();
        // Découpage du sleep pour réagir vite à un arrêt
        while (restant > 0 && actif) {
            Thread.sleep(Math.min(restant, 200)); // vérification toutes 200ms
            restant = getIntervalleMs() - (System.currentTimeMillis() - debut);
        }
    }
}