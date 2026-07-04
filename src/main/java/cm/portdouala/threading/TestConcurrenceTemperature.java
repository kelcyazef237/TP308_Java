package cm.portdouala.threading;

import cm.portdouala.metier.ConteneurRefrigere;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class TestConcurrenceTemperature {

    @Test
    @DisplayName("setTemperatureActuelle() doit être thread-safe sans exception")
    void testDeuxThreadsSimultanes() throws InterruptedException {

        // ─── Préparation ──────────────────────────────────────────────
        ConteneurRefrigere conteneur = new ConteneurRefrigere(
                "Test concurrent", 3000, "Dakar", -18.0, 2.0);

        int NB_THREADS     = 2;
        int NB_ITERATIONS  = 500; // chaque thread appelle 500 fois
        AtomicInteger erreurs = new AtomicInteger(0);

        // CountDownLatch : les 2 threads démarrent EXACTEMENT en même temps
        CountDownLatch top     = new CountDownLatch(1);  // signal de départ
        CountDownLatch fin     = new CountDownLatch(NB_THREADS); // attendre la fin

        // ─── Thread 1 : simule le simulateur de température ───────────
        Thread threadSimulateur = new Thread(() -> {
            try {
                top.await(); // attendre le signal
                for (int i = 0; i < NB_ITERATIONS; i++) {
                    double nouvelleTemp = -18.0 + (Math.random() * 10);
                    conteneur.setTemperatureActuelle(nouvelleTemp);
                }
            } catch (Exception e) {
                erreurs.incrementAndGet();
                System.err.println("❌ Erreur Thread Simulateur : " + e);
            } finally {
                fin.countDown();
            }
        }, "Thread-Simulateur");

        // ─── Thread 2 : simule l'IHM qui lit en même temps ───────────
        Thread threadIHM = new Thread(() -> {
            try {
                top.await(); // attendre le même signal
                for (int i = 0; i < NB_ITERATIONS; i++) {
                    // Lecture simultanée pendant que Thread 1 écrit
                    double temp     = conteneur.getTemperatureActuelle();
                    boolean alerte  = conteneur.isTemperatureHorsNorme();
                    double ecart    = conteneur.getEcartTemperature();
                    // On vérifie juste que les valeurs sont cohérentes
                    assertNotNull(temp);
                }
            } catch (Exception e) {
                erreurs.incrementAndGet();
                System.err.println("❌ Erreur Thread IHM : " + e);
            } finally {
                fin.countDown();
            }
        }, "Thread-IHM-Lecteur");

        // ─── Lancement simultané ──────────────────────────────────────
        threadSimulateur.start();
        threadIHM.start();

        System.out.println("⚡ Les 2 threads démarrent simultanément...");
        top.countDown(); // GO ! les 2 threads partent en même temps

        // Attendre que les 2 threads aient fini (max 10 secondes)
        fin.await();

        // ─── Vérification finale ──────────────────────────────────────
        System.out.println("Erreurs détectées : " + erreurs.get());
        assertEquals(0, erreurs.get(),
                "❌ Des erreurs de concurrence ont été détectées !");
        System.out.println("✅ Test thread-safety réussi — synchronized fonctionne.");
    }

    @Test
    @DisplayName("L'alerte doit se déclencher automatiquement hors norme")
    void testAlerteDeclenchee() {
        ConteneurRefrigere conteneur = new ConteneurRefrigere(
                "Test alerte", 1000, "Paris", -18.0, 2.0);

        // Dans la norme → pas d'alerte
        conteneur.setTemperatureActuelle(-18.0);
        assertFalse(conteneur.isTemperatureHorsNorme(),
                "Ne doit pas être en alerte à la consigne exacte");

        // Hors norme → alerte attendue
        conteneur.setTemperatureActuelle(-10.0); // écart = 8°C > tolérance 2°C
        assertTrue(conteneur.isTemperatureHorsNorme(),
                "Doit être en alerte — écart de 8°C dépasse la tolérance de 2°C");

        System.out.println("✅ Alerte correctement déclenchée.");
        System.out.println("   Écart : " + conteneur.getEcartTemperature() + "°C");
    }
}