package cm.portdouala.threading;

import cm.portdouala.metier.ConteneurRefrigere;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

class TestCallbackIHM {

    @Test
    @DisplayName("Le callback doit recevoir la liste après chaque cycle")
    void testCallbackReçuApresChaqueCycle() throws InterruptedException {

        // ─── Compteur de callbacks reçus ─────────────────────────────
        AtomicInteger nbCallbacksReçus = new AtomicInteger(0);

        // ─── Stocker la dernière liste reçue pour vérification ───────
        AtomicReference<List<ConteneurRefrigere>> derniereListeReçue =
                new AtomicReference<>(null);

        // ─── Latch : attendre au moins 2 callbacks (2 cycles) ────────
        CountDownLatch latch = new CountDownLatch(2);

        // ─── Définir le callback (ce que l'IHM fera de la liste) ─────
        Consumer<List<ConteneurRefrigere>> callbackTest = (liste) -> {
            // Vérifications sur la liste reçue
            assertNotNull(liste, "La liste reçue ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            // Vérifier que chaque conteneur a des données valides
            for (ConteneurRefrigere c : liste) {
                assertNotNull(c.getIdentifiant());
                assertNotNull(c.getDesignation());
                assertTrue(c.getTemperatureConsigne() >= -35.0);
                assertTrue(c.getTemperatureConsigne() <= 30.0);
            }

            derniereListeReçue.set(liste);
            nbCallbacksReçus.incrementAndGet();
            latch.countDown();

            System.out.printf("📩 Callback #%d reçu — %d conteneurs%n",
                    nbCallbacksReçus.get(), liste.size());
        };

        // ─── Lancer le simulateur avec ce callback ────────────────────
        SimulateurTemperature simulateur = new SimulateurTemperature(
                callbackTest,  // ← notre callback de test
                (alerte) -> System.out.println("🚨 Alerte : " + alerte.getMessage())
        );

        simulateur.demarrer();

        // ─── Attendre 2 cycles (max 10 secondes) ─────────────────────
        boolean reçu = latch.await(10, TimeUnit.SECONDS);

        simulateur.arreter();

        // ─── Vérifications finales ────────────────────────────────────
        assertTrue(reçu,
                "❌ Le callback n'a pas été reçu 2 fois en 10 secondes !");

        assertNotNull(derniereListeReçue.get(),
                "❌ La liste reçue est null !");

        assertTrue(nbCallbacksReçus.get() >= 2,
                "❌ Le callback doit être appelé au moins 2 fois");

        System.out.println("\n✅ Callback validé !");
        System.out.println("   Callbacks reçus    : " + nbCallbacksReçus.get());
        System.out.println("   Dernière liste      : "
                + derniereListeReçue.get().size() + " conteneurs");
    }

    @Test
    @DisplayName("Le callback ne doit pas recevoir de liste null ou modifiable")
    void testListeImmuable() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> erreurModification = new AtomicReference<>(null);

        Consumer<List<ConteneurRefrigere>> callbackTest = (liste) -> {
            try {
                // Tenter de modifier la liste → doit échouer
                liste.add(new ConteneurRefrigere(
                        "Intrus", 100, "Hack", -5.0, 1.0));
                // Si on arrive ici → la liste est modifiable → PROBLÈME
                System.err.println("⚠ La liste est modifiable — risque de bug !");
            } catch (UnsupportedOperationException e) {
                // Comportement attendu → liste immuable ✅
                System.out.println("✅ Liste immuable — modification rejetée.");
            } catch (Exception e) {
                erreurModification.set(e);
            } finally {
                latch.countDown();
            }
        };

        SimulateurTemperature simulateur = new SimulateurTemperature(
                callbackTest, null);

        simulateur.demarrer();
        latch.await(10, TimeUnit.SECONDS);
        simulateur.arreter();

        assertNull(erreurModification.get(),
                "Une exception inattendue s'est produite : "
                        + erreurModification.get());
    }
}