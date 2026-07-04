package cm.portdouala.ihm.service;

import fr.tp308.ihm.model.ConteneurDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IConteneurRepository {

    List<ConteneurDto> findAll();

    Optional<ConteneurDto> findById(UUID id);

    ConteneurDto save(ConteneurDto conteneur);

    ConteneurDto update(ConteneurDto conteneur);

    void delete(UUID id);

    List<ConteneurDto> findAlerts();

    void refresh();
}
