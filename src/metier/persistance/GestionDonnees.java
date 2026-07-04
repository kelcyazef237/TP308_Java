package metier.persistance;

import metier.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestionnaire central des donnees de fret portuaire.
 * Thread-safe : toutes les operations sur la liste sont synchronisees.
 *
 * @author Equipe Core & Metier
 * @version 1.0
 */
public class GestionDonnees implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FICHIER_DEFAUT = "port_douala.ser";
    private static final Random RANDOM = new Random();

    private List<Marchandise> cargaisons;
    private transient Object verrou;

    /** Historique des temperatures (ring buffer par conteneur). */
    private final Map<Integer, Deque<MesureTemperature>> historique = new ConcurrentHashMap<>();
    private static final int MAX_LECTURES = 60;

    public GestionDonnees() {
        this.cargaisons = new ArrayList<>();
        this.verrou = new Object();
    }

    /**
     * Reinitialise le verrou apres deserialisation (car transient).
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.verrou = new Object();
    }

    // ==================== HISTORIQUE TEMPERATURE ====================

    /**
     * Enregistre une mesure dans le ring buffer du conteneur (cap MAX_LECTURES).
     */
    public void enregistrerMesure(ConteneurRefrigere cr) {
        Deque<MesureTemperature> dq = historique.computeIfAbsent(cr.getId(), k -> new ArrayDeque<>(MAX_LECTURES));
        synchronized (dq) {
            if (dq.size() >= MAX_LECTURES) dq.pollFirst();
            dq.addLast(new MesureTemperature(System.currentTimeMillis(), cr.getTemperatureActuelle(), cr.estEnAlerte()));
        }
    }

    /** Snapshot des mesures pour un conteneur. */
    public List<MesureTemperature> getHistorique(int id) {
        Deque<MesureTemperature> dq = historique.get(id);
        if (dq == null) return List.of();
        synchronized (dq) {
            return new ArrayList<>(dq);
        }
    }

    /** Snapshot de tous les historiques. */
    public Map<Integer, Deque<MesureTemperature>> snapshotHistoriques() {
        Map<Integer, Deque<MesureTemperature>> res = new LinkedHashMap<>();
        for (Map.Entry<Integer, Deque<MesureTemperature>> e : historique.entrySet()) {
            Deque<MesureTemperature> dq = e.getValue();
            Deque<MesureTemperature> copy;
            synchronized (dq) {
                copy = new ArrayDeque<>(dq);
            }
            res.put(e.getKey(), copy);
        }
        return res;
    }

    // ==================== CRUD ====================

    public void ajouter(Marchandise m) {
        if (m == null) throw new IllegalArgumentException("La marchandise ne peut pas etre nulle.");
        synchronized (verrou) {
            cargaisons.add(m);
        }
    }

    public boolean supprimer(int id) {
        synchronized (verrou) {
            return cargaisons.removeIf(m -> m.getId() == id);
        }
    }

    public Optional<Marchandise> trouverParId(int id) {
        synchronized (verrou) {
            return cargaisons.stream().filter(m -> m.getId() == id).findFirst();
        }
    }

    public void modifier(Marchandise modifiee) {
        synchronized (verrou) {
            for (int i = 0; i < cargaisons.size(); i++) {
                if (cargaisons.get(i).getId() == modifiee.getId()) {
                    cargaisons.set(i, modifiee);
                    return;
                }
            }
        }
    }

    // ==================== REQUETES ====================

    public List<Marchandise> getToutes() {
        synchronized (verrou) {
            return new ArrayList<>(cargaisons);
        }
    }

    public List<ConteneurRefrigere> getEnAlerte() {
        synchronized (verrou) {
            return cargaisons.stream()
                    .filter(m -> m instanceof ConteneurRefrigere)
                    .map(m -> (ConteneurRefrigere) m)
                    .filter(ConteneurRefrigere::estEnAlerte)
                    .collect(Collectors.toList());
        }
    }

    public List<ConteneurRefrigere> getRefrigeres() {
        synchronized (verrou) {
            return cargaisons.stream()
                    .filter(m -> m instanceof ConteneurRefrigere)
                    .map(m -> (ConteneurRefrigere) m)
                    .collect(Collectors.toList());
        }
    }

    public List<Marchandise> getStandard() {
        synchronized (verrou) {
            return cargaisons.stream()
                    .filter(m -> m instanceof ConteneurStandard)
                    .collect(Collectors.toList());
        }
    }

    public int getNombreTotal() {
        synchronized (verrou) {
            return cargaisons.size();
        }
    }

    // ==================== STATISTIQUES ====================

    public Map<String, Object> getStatistiques() {
        synchronized (verrou) {
            Map<String, Object> stats = new LinkedHashMap<>();

            int total = cargaisons.size();
            long nbStandard = cargaisons.stream().filter(m -> m instanceof ConteneurStandard).count();
            long nbRefrigere = cargaisons.stream().filter(m -> m instanceof ConteneurRefrigere).count();
            long nbAlertes = cargaisons.stream()
                    .filter(m -> m instanceof ConteneurRefrigere)
                    .map(m -> (ConteneurRefrigere) m)
                    .filter(ConteneurRefrigere::estEnAlerte)
                    .count();

            double taxeTotale = cargaisons.stream().mapToDouble(Marchandise::calculerTaxe).sum();
            double taxeMoyenne = total > 0 ? taxeTotale / total : 0;

            double tempMoyenne = cargaisons.stream()
                    .filter(m -> m instanceof ConteneurRefrigere)
                    .map(m -> (ConteneurRefrigere) m)
                    .mapToDouble(ConteneurRefrigere::getTemperatureActuelle)
                    .average()
                    .orElse(0.0);

            double surchargeEnergetique = cargaisons.stream()
                    .filter(m -> m instanceof ConteneurRefrigere)
                    .mapToDouble(m -> m.getPoids() * 0.02)
                    .sum();

            long arriveesAujourdhui = cargaisons.stream()
                    .filter(m -> m.getDateArrivee() != null && m.getDateArrivee().equals(LocalDate.now()))
                    .count();

            stats.put("total", total);
            stats.put("standard", nbStandard);
            stats.put("refrigere", nbRefrigere);
            stats.put("alertes", nbAlertes);
            stats.put("taxeTotale", taxeTotale);
            stats.put("taxeMoyenne", taxeMoyenne);
            stats.put("temperatureMoyenne", tempMoyenne);
            stats.put("surchargeEnergetique", surchargeEnergetique);
            stats.put("arriveesAujourdhui", arriveesAujourdhui);

            return stats;
        }
    }

    // ==================== VARIATION TEMPERATURE ====================

    /**
     * Applique une variation aleatoire de temperature a chaque conteneur refrigere.
     * Enregistre aussi la mesure dans l'historique.
     * @return true si au moins un etat d'alerte a change
     */
    public boolean appliquerVariationTemperature() {
        synchronized (verrou) {
            boolean changement = false;
            for (Marchandise m : cargaisons) {
                if (m instanceof ConteneurRefrigere) {
                    ConteneurRefrigere cr = (ConteneurRefrigere) m;
                    double variation = (RANDOM.nextDouble() - 0.5) * 1.0; // -0.5 a +0.5
                    double nouvelleTemp = cr.getTemperatureActuelle() + variation;
                    // Limiter a des valeurs raisonnables
                    nouvelleTemp = Math.max(-30.0, Math.min(50.0, nouvelleTemp));
                    if (cr.mettreAJourTemperature(nouvelleTemp)) {
                        changement = true;
                        // Mettre a jour le statut
                        if (cr.estEnAlerte()) {
                            m.setStatut("Alerte");
                        } else {
                            m.setStatut("En transit");
                        }
                    }
                    // Toujours enregistrer la mesure (meme si l'etat n'a pas change)
                    enregistrerMesure(cr);
                }
            }
            return changement;
        }
    }

    // ==================== PERSISTANCE ====================

    public void sauvegarder() throws IOException {
        sauvegarder(FICHIER_DEFAUT);
    }

    public void sauvegarder(String chemin) throws IOException {
        synchronized (verrou) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
                oos.writeObject(new ArrayList<>(cargaisons));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void charger() throws IOException, ClassNotFoundException {
        charger(FICHIER_DEFAUT);
    }

    @SuppressWarnings("unchecked")
    public void charger(String chemin) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            List<Marchandise> chargees = (List<Marchandise>) ois.readObject();
            synchronized (verrou) {
                this.cargaisons = new ArrayList<>(chargees);
            }
        }
    }

    public boolean fichierExiste() {
        return new File(FICHIER_DEFAUT).exists();
    }

    public boolean fichierExiste(String chemin) {
        return new File(chemin).exists();
    }

    // ==================== DONNEES DEMO ====================

    /**
     * Cree des donnees de demonstration pour le premier lancement.
     */
    public void initialiserDonneesDemo() {
        synchronized (verrou) {
            if (!cargaisons.isEmpty()) return;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Conteneurs standards
            ConteneurStandard cs1 = new ConteneurStandard(1, 12500.0, "Materiaux de construction", "Ciment et acier");
            cs1.setNumeroConteneur("CONT-0001");
            cs1.setOrigine("Shanghai");
            cs1.setDestination("Douala");
            cs1.setDateArrivee(LocalDate.now().minusDays(2));
            cs1.setCompagnieMaritime("CMA-CGM");
            cs1.setStatut("Arrivee");
            cargaisons.add(cs1);

            ConteneurStandard cs2 = new ConteneurStandard(2, 8500.0, "Textiles", "Vetements et tissus");
            cs2.setNumeroConteneur("CONT-0002");
            cs2.setOrigine("Istanbul");
            cs2.setDestination("Douala");
            cs2.setDateArrivee(LocalDate.now().minusDays(1));
            cs2.setCompagnieMaritime("Maersk");
            cs2.setStatut("En transit");
            cargaisons.add(cs2);

            ConteneurStandard cs3 = new ConteneurStandard(3, 22000.0, "Machinerie lourde", "Equipements industriels");
            cs3.setNumeroConteneur("CONT-0003");
            cs3.setOrigine("Rotterdam");
            cs3.setDestination("Douala");
            cs3.setDateArrivee(LocalDate.now());
            cs3.setCompagnieMaritime("MSC");
            cs3.setStatut("Inspection");
            cargaisons.add(cs3);

            ConteneurStandard cs4 = new ConteneurStandard(4, 5000.0, "Produits electroniques", "Smartphones et accessoires");
            cs4.setNumeroConteneur("CONT-0004");
            cs4.setOrigine("Shenzhen");
            cs4.setDestination("Douala");
            cs4.setDateArrivee(LocalDate.now().minusDays(5));
            cs4.setCompagnieMaritime("COSCO");
            cs4.setStatut("Arrivee");
            cargaisons.add(cs4);

            // Conteneurs refrigeres
            ConteneurRefrigere cr1 = new ConteneurRefrigere(5, 6800.0, "Produits pharmaceutiques", 2.0, 8.0);
            cr1.setNumeroConteneur("CONT-0005");
            cr1.setOrigine("Paris");
            cr1.setDestination("Douala");
            cr1.setDateArrivee(LocalDate.now().minusDays(1));
            cr1.setCompagnieMaritime("CMA-CGM");
            cr1.setStatut("En transit");
            cargaisons.add(cr1);

            ConteneurRefrigere cr2 = new ConteneurRefrigere(6, 4200.0, "Poisson surgele", -25.0, -15.0);
            cr2.setNumeroConteneur("CONT-0006");
            cr2.setOrigine("Dakar");
            cr2.setDestination("Douala");
            cr2.setDateArrivee(LocalDate.now());
            cr2.setCompagnieMaritime("Grimaldi");
            cr2.setStatut("En transit");
            cargaisons.add(cr2);

            ConteneurRefrigere cr3 = new ConteneurRefrigere(7, 9100.0, "Fruits tropicaux", 4.0, 12.0);
            cr3.setNumeroConteneur("CONT-0007");
            cr3.setOrigine("Abidjan");
            cr3.setDestination("Douala");
            cr3.setDateArrivee(LocalDate.now().minusDays(3));
            cr3.setCompagnieMaritime("MSC");
            cr3.setStatut("En transit");
            cargaisons.add(cr3);

            ConteneurRefrigere cr4 = new ConteneurRefrigere(8, 3500.0, "Produits laitiers", 1.0, 6.0);
            cr4.setNumeroConteneur("CONT-0008");
            cr4.setOrigine("Anvers");
            cr4.setDestination("Douala");
            cr4.setDateArrivee(LocalDate.now());
            cr4.setCompagnieMaritime("Maersk");
            cr4.setStatut("Alerte");
            // Forcer une alerte
            cr4.mettreAJourTemperature(8.5);
            cargaisons.add(cr4);

            ConteneurStandard cs5 = new ConteneurStandard(9, 15000.0, "Pieces automobiles", "Moteurs et pneus");
            cs5.setNumeroConteneur("CONT-0009");
            cs5.setOrigine("Tokyo");
            cs5.setDestination("Douala");
            cs5.setDateArrivee(LocalDate.now().minusDays(4));
            cs5.setCompagnieMaritime("ONE");
            cs5.setStatut("Inspection");
            cargaisons.add(cs5);

            ConteneurStandard cs6 = new ConteneurStandard(10, 11200.0, "Meubles", "Bois et cuir");
            cs6.setNumeroConteneur("CONT-0010");
            cs6.setOrigine("Naples");
            cs6.setDestination("Douala");
            cs6.setDateArrivee(LocalDate.now().minusDays(1));
            cs6.setCompagnieMaritime("MSC");
            cs6.setStatut("En transit");
            cargaisons.add(cs6);

            ConteneurRefrigere cr5 = new ConteneurRefrigere(11, 5600.0, "Viande de boeuf", -18.0, -10.0);
            cr5.setNumeroConteneur("CONT-0011");
            cr5.setOrigine("Buenos Aires");
            cr5.setDestination("Douala");
            cr5.setDateArrivee(LocalDate.now().minusDays(2));
            cr5.setCompagnieMaritime("Hapag-Lloyd");
            cr5.setStatut("En transit");
            cargaisons.add(cr5);
        }
    }
}
