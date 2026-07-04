package cm.portdouala.ihm.service;

import fr.tp308.ihm.model.ConteneurDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MockConteneurService implements IConteneurService {

    private final IConteneurRepository repository;

    public MockConteneurService() {
        this.repository = new MockConteneurRepository();
    }

    @Override
    public List<ConteneurDto> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ConteneurDto> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public ConteneurDto save(ConteneurDto conteneur) {
        return repository.save(conteneur);
    }

    @Override
    public ConteneurDto update(ConteneurDto conteneur) {
        return repository.update(conteneur);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }

    @Override
    public List<ConteneurDto> findAlerts() {
        return repository.findAlerts();
    }

    @Override
    public void refresh() {
        repository.refresh();
    }
}
