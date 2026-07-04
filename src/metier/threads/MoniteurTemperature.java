package metier.threads;

import metier.persistance.GestionDonnees;

import javax.swing.*;

/**
 * Thread de surveillance qui met a jour aleatoirement la temperature
 * des conteneurs refrigeres toutes les 3 secondes.
 *
 * @author Equipe Core & Metier
 * @version 1.0
 */
public class MoniteurTemperature implements Runnable {

    private final GestionDonnees gestionDonnees;
    private final Runnable onActualisation;
    private volatile boolean actif = true;
    private static final int INTERVALLE_MS = 3000;

    public MoniteurTemperature(GestionDonnees gestionDonnees, Runnable onActualisation) {
        this.gestionDonnees = gestionDonnees;
        this.onActualisation = onActualisation;
    }

    public void arreter() {
        actif = false;
    }

    public boolean estActif() {
        return actif;
    }

    @Override
    public void run() {
        while (actif) {
            try {
                Thread.sleep(INTERVALLE_MS);

                if (!actif) break;

                // Appliquer les variations de temperature (thread-safe)
                boolean changement = gestionDonnees.appliquerVariationTemperature();

                // Toujours actualiser l'UI pour les timestamps et statistiques
                SwingUtilities.invokeLater(() -> {
                    if (onActualisation != null) {
                        onActualisation.run();
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
