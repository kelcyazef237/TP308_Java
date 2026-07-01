# IHM Swing - Gestion du trafic de fret portuaire

## Objectif du module

Ce module est l'interface graphique Swing dédiée à la gestion des conteneurs portuaires.
Il est conçu pour fonctionner de manière indépendante grâce à une couche d'abstraction de service et de repository.

## Architecture

Packages principaux :

- `fr.tp308.ihm` : point d'entrée de l'application.
- `fr.tp308.ihm.ui` : composants Swing (`MainFrame`, `FormPanel`, `TablePanel`, `StatusBar`, etc.).
- `fr.tp308.ihm.model` : objet de transfert de données `ConteneurDto`.
- `fr.tp308.ihm.service` : abstractions de service/repository et implementation mock.
- `fr.tp308.ihm.util` : utilitaires d'interface et thème.

### Composants clés

- `Main.java` : lance l'application Swing.
- `MainFrame.java` : assemble l'UI principale et gère les interactions.
- `FormPanel.java` : formulaire de saisie avec champs et boutons.
- `TablePanel.java` : `JTable` élégante pour afficher les conteneurs.
- `MarchandiseTableModel.java` : modèle de table pour afficher les données.
- `ConteneurDto.java` : objet de transfert indépendant de la logique métier.
- `IConteneurService.java` : interface de service utilisée par l'UI.
- `IConteneurRepository.java` : interface de repository pour la persistance.
- `MockConteneurRepository.java` / `MockConteneurService.java` : données fictives pour développement.
- `DialogUtils.java` : affichage standardisé des boîtes de dialogue.
- `AppTheme.java` : constantes de style et apparence.

## Intégration future

Le module IHM est prêt à être intégré avec les équipes Core/Métier, Persistance et Multithreading.

### Remplacement de la couche mock

1. Implémenter `IConteneurRepository` dans le module Persistance ou Core.
2. Remplacer l'instance `MockConteneurRepository` par la nouvelle implémentation dans `MockConteneurService` ou créer un nouveau service réel.
3. S'assurer que le nouveau service implémente `IConteneurService`.

### Points d'injection

- `Main.main()` instancie actuellement `MockConteneurService`.
- `MainFrame` reçoit `IConteneurService` en paramètre.
- Toutes les opérations UI (`Ajouter`, `Modifier`, `Supprimer`, `Actualiser`, `Afficher uniquement les alertes`) passent par `IConteneurService`.

### Modification attendue

La migration se fera sans modifier les composants Swing :

- les méthodes `findAll()`, `save(...)`, `update(...)`, `delete(...)`, `findAlerts()`, `refresh()` sont les points d'extension.
- `ConteneurDto` permet de conserver l'isolation entre l'IHM et le domaine métier.

## Exécution

### Compilation

```bash
cd '/home/bayi-ryan/Bureau/Tp final 308/TP308_Java-equipe-IHM-Swing'
find src -name '*.java' -print0 | xargs -0 javac -d out
```

### Lancement

```bash
cd '/home/bayi-ryan/Bureau/Tp final 308/TP308_Java-equipe-IHM-Swing'
java -cp out fr.tp308.ihm.Main
```

## Notes importantes

- Aucune logique métier n'est implémentée dans l'UI.
- Les calculs de taxe et la persistance réelle doivent rester du ressort des autres équipes.
- L'UI utilise des données fictives pour permettre un développement indépendant et un test visuel immédiat.

## Propositions d'améliorations futures

- Ajouter une couche de validation partagée si nécessaire.
- Remplacer `MockConteneurService` par un service réel dans une configuration de production.
- Ajouter un mécanisme de dépendance (par exemple injection via `ServiceLoader` ou une configuration simple) pour sélectionner entre mock et réel.
