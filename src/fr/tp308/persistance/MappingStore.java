package fr.tp308.persistance;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stocke la correspondance entre les identifiants métier (int) et les
 * identifiants IHM (UUID), ainsi que les métadonnées absentes du modèle
 * métier (code, plage de température).
 *
 * Persisté indépendamment de GestionPortuaire (fichier "mapping.ser"),
 * pour ne jamais avoir à modifier la classe Marchandise de l'équipe Core.
 *
 * @author Equipe Persistance
 */
public class MappingStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<UUID, ConteneurMeta> byUuid = new HashMap<>();
    private final Map<Integer, UUID> uuidByIdMetier = new HashMap<>();
    private int nextIdMetier = 1;

    /** Génère et réserve le prochain id métier (int) disponible. */
    public synchronized int genererNouvelIdMetier() {
        return nextIdMetier++;
    }

    public synchronized void enregistrer(ConteneurMeta meta) {
        byUuid.put(meta.getUuid(), meta);
        uuidByIdMetier.put(meta.getIdMetier(), meta.getUuid());
    }

    public synchronized ConteneurMeta getByUuid(UUID uuid) {
        return byUuid.get(uuid);
    }

    public synchronized ConteneurMeta getByIdMetier(int idMetier) {
        UUID uuid = uuidByIdMetier.get(idMetier);
        return uuid == null ? null : byUuid.get(uuid);
    }

    public synchronized void supprimer(UUID uuid) {
        ConteneurMeta meta = byUuid.remove(uuid);
        if (meta != null) {
            uuidByIdMetier.remove(meta.getIdMetier());
        }
    }

    /** Sauvegarde dans un fichier via sérialisation Java. */
    public void sauvegarder(String chemin) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
            oos.writeObject(this);
        }
    }

    /** Charge depuis un fichier, ou retourne un store vide si le fichier n'existe pas encore. */
    public static MappingStore charger(String chemin) throws IOException, ClassNotFoundException {
        File f = new File(chemin);
        if (!f.exists()) {
            return new MappingStore();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            return (MappingStore) ois.readObject();
        }
    }
}
