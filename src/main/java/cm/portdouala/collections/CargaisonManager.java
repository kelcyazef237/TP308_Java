package cm.portdouala.collections;

import cm.portdouala.metier.ConteneurRefrigere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * STUB TEMPORAIRE — À remplacer par la version de l'équipe Core & Métier.
 * Singleton minimal avec uniquement ce dont Threading a besoin.
 */
public class CargaisonManager {

    private static volatile CargaisonManager instance;
    private final List<ConteneurRefrigere> refrigeres = new ArrayList<>();

    private CargaisonManager() {
        // Données de test pour que le simulateur ait quelque chose à simuler
        refrigeres.add(new ConteneurRefrigere(
                "Crevettes Atlantique", 5000, "Dakar", -18.0, 2.0));
        refrigeres.add(new ConteneurRefrigere(
                "Vaccins OMS", 1200, "Bruxelles", 4.0, 1.0));
        refrigeres.add(new ConteneurRefrigere(
                "Produits laitiers", 7800, "Paris", 6.0, 2.5));
    }

    public static CargaisonManager getInstance() {
        if (instance == null) {
            synchronized (CargaisonManager.class) {
                if (instance == null) instance = new CargaisonManager();
            }
        }
        return instance;
    }

    // ─── La seule méthode dont Threading a besoin ─────────────────────

    public synchronized List<ConteneurRefrigere> getConteneurRefrigeres() {
        return Collections.unmodifiableList(new ArrayList<>(refrigeres));
    }

    // ─── Utilitaire pour tes tests ────────────────────────────────────
    public synchronized void ajouterStub(ConteneurRefrigere c) {
        refrigeres.add(c);
    }

    public synchronized int getTaille() {
        return refrigeres.size();
    }
}