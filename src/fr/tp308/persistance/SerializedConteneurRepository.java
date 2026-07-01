package fr.tp308.persistance;

import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.service.IConteneurRepository;
import metier.ConteneurRefrigere;
import metier.ConteneurStandard;
import metier.GestionPortuaire;
import metier.Marchandise;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation de IConteneurRepository basée sur la sérialisation Java.
 *
 * Persiste les données métier (Marchandise/ConteneurStandard/ConteneurRefrigere)
 * dans "fret.ser" via GestionPortuaire, et les métadonnées IHM (UUID, code,
 * plage de température) dans "mapping.ser" via MappingStore.
 *
 * Règle importante : alerte et taxe ne sont JAMAIS lues depuis le DTO en
 * entrée (save/update). Elles sont toujours recalculées à partir des objets
 * métier (calculerTaxe(), estEnAlerte()), qui restent la seule source de
 * vérité pour ces valeurs dérivées.
 *
 * @author Equipe Persistance
 */
public class SerializedConteneurRepository implements IConteneurRepository {

    private static final String FICHIER_CARGAISONS = "fret.ser";
    private static final String FICHIER_MAPPING = "mapping.ser";
    private static final double ECART_TEMP_PAR_DEFAUT = 5.0;

    private GestionPortuaire gestion;
    private MappingStore mapping;

    public SerializedConteneurRepository() {
        chargerDepuisDisque();
    }

    // ---------------------------------------------------------------
    // Chargement / sauvegarde bas niveau
    // ---------------------------------------------------------------

    private void chargerDepuisDisque() {
        try {
            gestion = new java.io.File(FICHIER_CARGAISONS).exists()
                    ? GestionPortuaire.charger(FICHIER_CARGAISONS)
                    : new GestionPortuaire();
            mapping = MappingStore.charger(FICHIER_MAPPING);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erreur de chargement des données persistées", e);
        }
    }

    private void persister() {
        try {
            gestion.sauvegarder(FICHIER_CARGAISONS);
            mapping.sauvegarder(FICHIER_MAPPING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur de sauvegarde des données", e);
        }
    }

    // ---------------------------------------------------------------
    // Mapping Marchandise <-> ConteneurDto
    // ---------------------------------------------------------------

    private ConteneurDto versDto(Marchandise m) {
        ConteneurMeta meta = mapping.getByIdMetier(m.getId());
        if (meta == null) {
            throw new IllegalStateException(
                    "Aucune métadonnée trouvée pour le conteneur métier id=" + m.getId()
                    + " (fret.ser et mapping.ser désynchronisés)");
        }

        boolean alerte = false;
        Double temperature = null;
        if (m instanceof ConteneurRefrigere) {
            ConteneurRefrigere cr = (ConteneurRefrigere) m;
            alerte = cr.estEnAlerte();
            temperature = cr.getTemperatureActuelle();
        }

        String type = (m instanceof ConteneurRefrigere) ? "Réfrigéré" : "Standard";

        return new ConteneurDto(
                meta.getUuid(),
                type,
                meta.getCode(),
                m.getPoids(),
                temperature,
                alerte,
                m.calculerTaxe() // toujours recalculée, jamais lue depuis un DTO d'entrée
        );
    }

    private Marchandise construireMarchandise(int idMetier, ConteneurDto dto,
                                               Double tempMin, Double tempMax) {
        String description = dto.getCode(); // pas de champ description distinct côté DTO
        if ("Réfrigéré".equalsIgnoreCase(dto.getType())) {
            double min = tempMin != null ? tempMin
                    : (dto.getTemperature() != null ? dto.getTemperature() - ECART_TEMP_PAR_DEFAUT : -5.0);
            double max = tempMax != null ? tempMax
                    : (dto.getTemperature() != null ? dto.getTemperature() + ECART_TEMP_PAR_DEFAUT : 5.0);
            double init = dto.getTemperature() != null ? dto.getTemperature() : (min + max) / 2;
            return new ConteneurRefrigere(idMetier, dto.getPoids(), description, min, max, init);
        } else {
            // "contenu" du ConteneurStandard : pas de champ dédié côté DTO, on réutilise le code.
            return new ConteneurStandard(idMetier, dto.getPoids(), description, description);
        }
    }

    // ---------------------------------------------------------------
    // IConteneurRepository
    // ---------------------------------------------------------------

    @Override
    public synchronized List<ConteneurDto> findAll() {
        return gestion.getToutesLesCargaisons().stream()
                .map(this::versDto)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Optional<ConteneurDto> findById(UUID id) {
        ConteneurMeta meta = mapping.getByUuid(id);
        if (meta == null) {
            return Optional.empty();
        }
        return gestion.getToutesLesCargaisons().stream()
                .filter(m -> m.getId() == meta.getIdMetier())
                .findFirst()
                .map(this::versDto);
    }

    @Override
    public synchronized ConteneurDto save(ConteneurDto conteneur) {
        int idMetier = mapping.genererNouvelIdMetier();
        UUID uuid = UUID.randomUUID();

        Marchandise m = construireMarchandise(idMetier, conteneur, null, null);
        gestion.ajouterCargaison(m);

        Double tMin = (m instanceof ConteneurRefrigere) ? ((ConteneurRefrigere) m).getTemperatureMin() : null;
        Double tMax = (m instanceof ConteneurRefrigere) ? ((ConteneurRefrigere) m).getTemperatureMax() : null;
        mapping.enregistrer(new ConteneurMeta(uuid, idMetier, conteneur.getCode(), tMin, tMax));

        persister();
        return versDto(m);
    }

    @Override
    public synchronized ConteneurDto update(ConteneurDto conteneur) {
        ConteneurMeta meta = mapping.getByUuid(conteneur.getId());
        if (meta == null) {
            throw new NoSuchElementException("Conteneur introuvable pour id=" + conteneur.getId());
        }

        // Marchandise est essentiellement immuable : on remplace l'instance
        // tout en conservant le même id métier et la même UUID.
        gestion.supprimerCargaison(meta.getIdMetier());
        Marchandise nouveau = construireMarchandise(
                meta.getIdMetier(), conteneur, meta.getTemperatureMin(), meta.getTemperatureMax());
        gestion.ajouterCargaison(nouveau);

        meta.setCode(conteneur.getCode());
        if (nouveau instanceof ConteneurRefrigere) {
            ConteneurRefrigere cr = (ConteneurRefrigere) nouveau;
            meta.setTemperatureMin(cr.getTemperatureMin());
            meta.setTemperatureMax(cr.getTemperatureMax());
        }
        mapping.enregistrer(meta);

        persister();
        return versDto(nouveau);
    }

    @Override
    public synchronized void delete(UUID id) {
        ConteneurMeta meta = mapping.getByUuid(id);
        if (meta == null) {
            return;
        }
        gestion.supprimerCargaison(meta.getIdMetier());
        mapping.supprimer(id);
        persister();
    }

    @Override
    public synchronized List<ConteneurDto> findAlerts() {
        return gestion.getCargaisonsEnAlerte().stream()
                .map(m -> (Marchandise) m)
                .map(this::versDto)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void refresh() {
        chargerDepuisDisque();
    }
}
