# Gestion du Fret Portuaire (Port Autonome de Douala)

Ce projet est une application Java desktop conçue pour la gestion et le suivi des cargaisons au sein d'un port. L'application met en pratique les principes fondamentaux de la programmation orientée objet (POO), le développement d'interfaces graphiques interactives avec Swing (via FlatLaf), le multithreading, ainsi que la persistance des données.

## 🚀 Fonctionnalités Principales

- **Modélisation POO Complète** : Gestion des conteneurs standards et réfrigérés avec héritage, classes abstraites et calcul polymorphique des taxes douanières.
- **Interface Utilisateur Moderne** : Interface graphique (GUI) élégante construite avec Java Swing et **FlatLaf**, supportant le basculement dynamique à chaud entre le **Mode Clair** et le **Mode Sombre**.
- **Surveillance en Temps Réel** : Simulation de variations de température via un thread d'arrière-plan dédié (`MoniteurTemperature`), avec déclenchement visuel d'alertes en cas de dépassement des seuils critiques.
- **Persistance des Données** : Sauvegarde et restauration automatiques de l'état complet du port via la sérialisation binaire Java (`port_douala.ser`).
- **Tableau de Bord Analytique** : Statistiques en temps réel (nombre de conteneurs, taxes totales calculées, conteneurs en alerte) avec graphiques de suivi thermique.
- **Robustesse & Validation** : Protection complète des saisies utilisateur avec messages d'erreurs clairs et interception des exceptions métier.

## 🏗️ Architecture du Projet

Le projet est structuré en plusieurs packages pour séparer clairement la logique métier de l'interface graphique :

- `metier` : Contient le cœur de l'application (les classes `Marchandise`, `ConteneurStandard`, `ConteneurRefrigere`, `GestionPortuaire`).
- `metier.persistance` : Gestion de la sauvegarde des données et conservation de l'historique thermique.
- `metier.threads` : Multithreading pour la mise à jour asynchrone des températures sans bloquer l'interface principale (EDT).
- `metier.ui` : Ensemble des vues, fenêtres, formulaires, et gestion de la thématique.

## 🛠️ Prérequis

- **Java Development Kit (JDK) 17** ou supérieur.
- **Make** (optionnel, pour simplifier la compilation).

## ⚙️ Compilation et Exécution

Un fichier `Makefile` est fourni à la racine du projet pour faciliter l'exécution de l'application depuis un terminal.

### 1. Compiler le projet
```bash
make compile
```

### 2. Lancer l'application
```bash
make run
```

### 3. Nettoyer les fichiers compilés
```bash
make clean
```

*(Si vous n'utilisez pas Make, l'application peut également être compilée manuellement avec `javac` ou importée directement dans un IDE classique tel qu'IntelliJ IDEA ou Eclipse).*

## 🧪 Tests Unitaires

Le projet inclut une classe de test (`TestMetier.java`) vérifiant le comportement du "Core" métier :
- Les validations d'instanciation (poids négatifs, températures incohérentes).
- Les calculs exacts des taxes selon les formules polymorphes.
- Le déclenchement et l'extinction correcte des alertes de température.
- Le succès des opérations de sérialisation.

## 👥 Auteur
Développé par l'Équipe Core & Métier dans le cadre de l'évaluation académique (TP308).
