package fr.tp308.ihm.service;

import fr.tp308.ihm.model.ConteneurDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MockConteneurRepository implements IConteneurRepository {

    private final List<ConteneurDto> conteneurs;

    public MockConteneurRepository() {
        this.conteneurs = new ArrayList<>();
        initializeSampleData();
    }

    @Override
    public List<ConteneurDto> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(conteneurs));
    }

    @Override
    public Optional<ConteneurDto> findById(UUID id) {
        return conteneurs.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    @Override
    public ConteneurDto save(ConteneurDto conteneur) {
        ConteneurDto candidate = new ConteneurDto(
                UUID.randomUUID(),
                conteneur.getType(),
                conteneur.getCode(),
                conteneur.getPoids(),
                conteneur.getTemperature(),
                conteneur.isAlerte(),
                conteneur.getTaxe()
        );
        conteneurs.add(candidate);
        return candidate;
    }

    @Override
    public ConteneurDto update(ConteneurDto conteneur) {
        findById(conteneur.getId()).ifPresent(existing -> {
            existing.setType(conteneur.getType());
            existing.setCode(conteneur.getCode());
            existing.setPoids(conteneur.getPoids());
            existing.setTemperature(conteneur.getTemperature());
            existing.setAlerte(conteneur.isAlerte());
            existing.setTaxe(conteneur.getTaxe());
        });
        return conteneur;
    }

    @Override
    public void delete(UUID id) {
        conteneurs.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public List<ConteneurDto> findAlerts() {
        return conteneurs.stream()
                .filter(ConteneurDto::isAlerte)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void refresh() {
        // Nothing to refresh in mock repository. Keep method for integration.
    }

    private void initializeSampleData() {
        conteneurs.add(new ConteneurDto(UUID.randomUUID(), "Standard", "C001", 1200.0, null, false, 0.0));
        conteneurs.add(new ConteneurDto(UUID.randomUUID(), "Réfrigéré", "R101", 900.0, -4.0, true, 0.0));
        conteneurs.add(new ConteneurDto(UUID.randomUUID(), "Standard", "C002", 1500.0, null, false, 0.0));
    }
}
