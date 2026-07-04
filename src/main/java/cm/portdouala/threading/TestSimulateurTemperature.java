package cm.portdouala.threading;

import cm.portdouala.collections.CargaisonManager;
import cm.portdouala.metier.*;
import cm.portdouala.exceptions.TemperatureHorsNormeException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test d'intégration standalone (sans JUnit, sans IHM).
 * Lance la simulation pendant 15 secondes et affiche les résultats.
 */
public class TestSimulateurTemperature {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== TEST SIMULATEUR TEMPÉRATURE ===\n");

        // 1. Peupler le CargaisonManager avec des données de test
        CargaisonManager mgr = CargaisonManager.getInstance();

        ConteneurRefrigere r1 = new ConteneurRefrigere(
                "Crevettes test", 3000, TypeMarchandise.ALIMENTAIRE,
                "Dakar", -18.0, 2.0);

        ConteneurRefrigere r2 = new ConteneurRefrigere(
                "Vaccins test", 800, TypeMarchandise.AUTRE,
                "Paris", 4.0, 1.0); // Tolérance stricte → alertes fréquentes

        mgr.ajouter(r1);
        mgr.ajouter(r2);

        // 2. CountDownLatch pour attendre les alertes
        CountDownLatch latchAlertes = new CountDownLatch(3); // attend 3 alertes

        // 3. Créer le simulateur avec callbacks de test
        SimulateurTemperature simulateur = new SimulateurTemperature(
                // Callback mise à jour IHM → ici on imprime dans la console
                (List<ConteneurRefrigere> liste) -> {
                    System.out.printf("[CYCLE] %d conteneurs | Stats: %s%n",
                            liste.size(),
                            StatistiquesSimulation.getInstance());
                    for (ConteneurRefrigere c : liste) {
                        System.out.printf(
                                "  → [%s] Consigne:%.1f°C | Actuelle:%.1f°C | Écart:%+.1f°C | %s%n",
                                c.getIdentifiant(),
                                c.getTemperatureConsigne(),
                                c.getTemperatureActuelle(),
                                c.getEcartTemperature(),
                                c.isTemperatureHorsNorme() ? "⚠ ALERTE" : "✓ OK"
                        );
                    }
                },
                // Callback alerte
                (TemperatureHorsNormeException alerte) -> {
                    System.out.println("🚨 ALERTE : " + alerte.getMessage());
                    latchAlertes.countDown();
                }
        );

        // 4. Démarrer
        simulateur.demarrer();
        System.out.println("Simulation démarrée — 15 secondes de test...\n");

        // 5. Attendre 15 secondes (5 cycles de 3s)
        Thread.sleep(15_000);

        // 6. Test pause/reprise
        System.out.println("\n--- Test PAUSE ---");
        simulateur.mettreEnPause();
        Thread.sleep(4_000);
        System.out.println("--- Test REPRISE ---");
        simulateur.reprendre();
        Thread.sleep(6_000);

        // 7. Arrêt propre
        simulateur.arreter();
        System.out.println("\nSimulation arrêtée.");
        System.out.println("Stats finales : " + StatistiquesSimulation.getInstance());

        // 8. Vérification des alertes
        boolean alertesReçues = latchAlertes.await(0, TimeUnit.SECONDS);
        System.out.println("Alertes reçues (≥3) : "
                + (alertesReçues ? "✓ OUI" : "⚠ Pas encore — normal si conteneurs stables"));
    }
}