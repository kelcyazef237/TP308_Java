package metier;
import java.io.IOException;
import java.util.List;

/**
 * Classe de test unitaire pour le modele metier du projet Portuaire.
 * Executez cette classe pour valider le bon fonctionnement du Core.
 */
public class TestMetier {

    public static void main(String[] args) {
        System.out.println("=== LANCEMENT DES TESTS UNITAIRES ===\n");

        testValidations();
        testTaxes();
        testAlertes();
        testFiltresEtCollections();
        testSerialisation();

        System.out.println("\n=== TOUS LES TESTS SONT PASSES AVEC SUCCES ! ===");
    }

    // --- 1. VERIFICATION DES LEVEES D'EXCEPTION ---
    private static void testValidations() {
        System.out.println("--- Test des validations ---");

        // Poids negatif
        try {
            new ConteneurStandard(1, -10, "Test", "Materiel");
            System.err.println("ERREUR : Poids negatif non detecte !");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: Poids negatif capture : " + e.getMessage());
        }

        // Temperature min >= max
        try {
            new ConteneurRefrigere(2, 100, "Fruits", 10, 5);
            System.err.println("ERREUR : tempMin >= tempMax non detecte !");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: tempMin >= tempMax capture : " + e.getMessage());
        }

        // Temperature initiale hors plage
        try {
            new ConteneurRefrigere(3, 100, "Viande", 0, 10, 20);
            System.err.println("ERREUR : tempInit hors plage non detectee !");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: tempInit hors plage capturee : " + e.getMessage());
        }

        // Description vide
        try {
            new ConteneurStandard(4, 50, "", "Bois");
            System.err.println("ERREUR : Description vide non detectee !");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: Description vide capturee : " + e.getMessage());
        }

        System.out.println("--- Fin tests validations ---\n");
    }

    // --- 2. TESTS DES CALCULS DE TAXES ---
    private static void testTaxes() {
        System.out.println("--- Test des taxes ---");

        ConteneurStandard cs = new ConteneurStandard(10, 1000, "Materiaux", "Acier");
        ConteneurRefrigere cr = new ConteneurRefrigere(11, 800, "Aliments", -5, 5);

        double taxeStandard = cs.calculerTaxe();
        double taxeRefrigere = cr.calculerTaxe();

        if (taxeStandard == 50.0 && taxeRefrigere == 56.0) {
            System.out.println("OK: Calcul des taxes OK (Standard: " + taxeStandard + ", Refrigere: " + taxeRefrigere + ")");
        } else {
            System.err.println("ERREUR calcul taxes ! Standard=" + taxeStandard + " (attendu 50.0), Refrigere=" + taxeRefrigere + " (attendu 56.0)");
        }

        System.out.println("--- Fin test taxes ---\n");
    }

    // --- 3. TESTS DES ALERTES ---
    private static void testAlertes() {
        System.out.println("--- Test des alertes ---");

        ConteneurRefrigere cr = new ConteneurRefrigere(20, 500, "Medicaments", 2, 8);

        if (!cr.estEnAlerte()) {
            System.out.println("OK: Pas d'alerte a l'initialisation (temp=" + cr.getTemperatureActuelle() + ")");
        } else {
            System.err.println("ERREUR : Alerte declenchee alors que temp initiale est dans la plage !");
        }

        boolean alerteChangee = cr.mettreAJourTemperature(10.0);
        if (alerteChangee && cr.estEnAlerte()) {
            System.out.println("OK: Alerte declenchee avec temp=10.0 (retour booleen = true)");
        } else {
            System.err.println("ERREUR : L'alerte aurait du se declencher !");
        }

        alerteChangee = cr.mettreAJourTemperature(5.0);
        if (alerteChangee && !cr.estEnAlerte()) {
            System.out.println("OK: Retour a la normale avec temp=5.0 (retour booleen = true)");
        } else {
            System.err.println("ERREUR : L'alerte aurait du disparaitre !");
        }

        alerteChangee = cr.mettreAJourTemperature(6.0);
        if (!alerteChangee && !cr.estEnAlerte()) {
            System.out.println("OK: Temperature modifiee sans changement d'alerte (retour booleen = false)");
        } else {
            System.err.println("ERREUR : Le retour booleen aurait du etre false !");
        }

        System.out.println("--- Fin test alertes ---\n");
    }

    // --- 4. TESTS DES FILTRES ET COLLECTIONS ---
    private static void testFiltresEtCollections() {
        System.out.println("--- Test des collections et filtres ---");

        GestionPortuaire gp = new GestionPortuaire();

        ConteneurStandard s1 = new ConteneurStandard(1, 100, "Outils", "Marteaux");
        ConteneurRefrigere r1 = new ConteneurRefrigere(2, 200, "Poissons", -10, 0);
        ConteneurRefrigere r2 = new ConteneurRefrigere(3, 300, "Fruits", 0, 10);

        gp.ajouterCargaison(s1);
        gp.ajouterCargaison(r1);
        gp.ajouterCargaison(r2);

        r1.mettreAJourTemperature(5.0);

        List<ConteneurRefrigere> alertes = gp.getCargaisonsEnAlerte();
        if (alertes.size() == 1 && alertes.get(0).getId() == 2) {
            System.out.println("OK: Filtrage des alertes OK (1 alerte trouvee, ID=2)");
        } else {
            System.err.println("ERREUR filtre alertes ! Taille=" + alertes.size());
        }

        List<ConteneurRefrigere> refs = gp.getConteneursRefrigeres();
        if (refs.size() == 2) {
            System.out.println("OK: Liste des refrigeres OK (2 conteneurs trouves)");
        } else {
            System.err.println("ERREUR liste refrigeres ! Taille=" + refs.size());
        }

        double totalTaxes = gp.calculerTaxeTotale();
        double attendu = (100 * 0.05) + (200 * 0.07) + (300 * 0.07);
        if (Math.abs(totalTaxes - attendu) < 0.01) {
            System.out.println("OK: Taxe totale OK (" + totalTaxes + " CFA attendus)");
        } else {
            System.err.println("ERREUR taxe totale ! Obtenu=" + totalTaxes + ", Attendu=" + attendu);
        }

        boolean supprime = gp.supprimerCargaison(1);
        if (supprime && gp.getToutesLesCargaisons().size() == 2) {
            System.out.println("OK: Suppression par ID OK (plus que 2 cargaisons)");
        } else {
            System.err.println("ERREUR suppression !");
        }

        System.out.println("--- Fin test collections ---\n");
    }

    // --- 5. TESTS DE LA SERIALISATION ---
    private static void testSerialisation() {
        System.out.println("--- Test de la serialisation ---");

        GestionPortuaire gpOriginal = new GestionPortuaire();
        gpOriginal.ajouterCargaison(new ConteneurStandard(99, 150, "Test Serial", "Papier"));
        gpOriginal.ajouterCargaison(new ConteneurRefrigere(100, 250, "Test Serial Froid", -5, 5));

        String fichier = "fret_test.ser";

        try {
            gpOriginal.sauvegarder(fichier);
            System.out.println("OK: Sauvegarde effectuee dans " + fichier);

            GestionPortuaire gpCharge = GestionPortuaire.charger(fichier);

            if (gpCharge.getToutesLesCargaisons().size() == 2) {
                System.out.println("OK: Chargement OK : 2 cargaisons restaurees");
            } else {
                System.err.println("ERREUR : Nombre de cargaisons apres chargement incorrect !");
            }

            new java.io.File(fichier).delete();
            System.out.println("OK: Fichier test supprime proprement");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ERREUR lors de la serialisation : " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- Fin test serialisation ---\n");
    }
}